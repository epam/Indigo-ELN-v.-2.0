/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.file.GridFSFileUtil;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.exception.*;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.epam.indigoeln.web.rest.util.permission.helpers.PermissionChanges;
import com.epam.indigoeln.web.rest.util.permission.helpers.ProjectPermissionHelper;
import com.mongodb.DBRef;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epam.indigoeln.core.util.WebSocketUtil.getEntityUpdateRecipients;
import static com.epam.indigoeln.core.util.WebSocketUtil.getSubEntityChangesRecipients;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Provides a number of methods for access to project's data in database.
 */
@Service
public class ProjectService {

    /**
     * Instance of ProjectRepository for access to projects in database.
     */
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Instance of NotebookRepository for access to notebooks in database.
     */
    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    /**
     * Instance of FileRepository for access to files in database.
     */
    @Autowired
    private FileRepository fileRepository;

    /**
     * Instance of UserService for access to users in database.
     */
    @Autowired
    private UserService userService;

    /**
     * Instance of CustomDtoMapper for conversion from dto object.
     */
    @Autowired
    private CustomDtoMapper mapper;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private WebSocketUtil webSocketUtil;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Returns all projects without checking for UserPermissions.
     *
     * @return List with projects
     */
    public List<TreeNodeDTO> getAllProjectsAsTreeNodes() {
        return getAllProjectsAsTreeNodes(null);
    }

    /**
     * If user is null, then retrieve projects without checking for UserPermissions.
     * Otherwise, use checking for UserPermissions.
     *
     * @param user User
     * @return Projects ass tree nodes
     */
    public List<TreeNodeDTO> getAllProjectsAsTreeNodes(User user) {
        // if user is null, then get all projects

        val collection = mongoTemplate.getCollection(Project.COLLECTION_NAME);

        val projects = (user == null) ? collection.find()
                : collection.find(new Document()
                .append("accessList.user", new Document()
                        .append("$ref", User.COLLECTION_NAME).append("$id", user.getId())));

        return StreamSupport.stream(projects.spliterator(), false)
                .map(TreeNodeDTO::new)
                .sorted(TreeNodeDTO.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    public TreeNodeDTO getProjectAsTreeNode(String projectId) {
        TreeNodeDTO result;
        val collection = mongoTemplate.getCollection(Project.COLLECTION_NAME);
        Document searchQuery = new Document();
        searchQuery.put("_id", projectId);
        val project = collection.find(searchQuery).first();
        if (project != null) {
            result = new TreeNodeDTO(project);
        } else {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }
        return result;
    }


    /**
     * Returns project by id according to permissions.
     *
     * @param id   Project's identifier
     * @param user User
     * @return Project by id
     */
    public ProjectDTO getProjectById(String id, User user) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        Project project = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(id));

        // Check of EntityAccess (User must have "Read Entity" permission in project's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createProjectReadOperation(project.getId());
        }
        return new ProjectDTO(project);
    }

    /**
     * Fetch all projects for current user available when create sub entity
     * return notebooks where users is in accessList and has permission CREATE_SUB_ENTITY,
     * or has authority CONTENT_EDITOR (but not in accessLIst).
     *
     * @param user user
     * @return list of projects available for notebook creation
     */
    public List<ShortEntityDTO> getProjectsForNotebookCreation(User user) {
        val projectCollection = mongoTemplate.getCollection(Project.COLLECTION_NAME);
        val userCollection = mongoTemplate.getCollection(User.COLLECTION_NAME);

        val projects = new HashMap<Object, Document>();

        if (PermissionUtil.isContentEditor(user)) {
            projectCollection
                    .find()
                    .forEach(p -> projects.put(p.get("_id"), p));
        } else {
            projectCollection
                    .find(new Document()
                            .append("accessList", new Document()
                                    .append("$elemMatch", new Document()
                                            .append("user.$id", user.getId())
                                            .append("permissions", new Document()
                                                    .append("$in", Collections.singletonList(UserPermission.CREATE_SUB_ENTITY))))))
                    .forEach(p -> projects.put(p.get("_id"), p));
        }

        val userIds = new HashSet<Object>();
        projects
                .values()
                .forEach(p -> {
                    if (p.get("author") != null) {
                        userIds.add(((DBRef) p.get("author")).getId());
                    }
                    if (p.get("lastModifiedBy") != null) {
                        userIds.add(((DBRef) p.get("lastModifiedBy")).getId());
                    }
                });

        val users = new HashMap<Object, Document>();
        userCollection
                .find(new Document("_id", new Document("$in", userIds)))
                .forEach(u -> users.put(u.get("_id"), u));

        return projects
                .values()
                .stream()
                .map(p -> new ShortEntityDTO(p, users))
                .sorted(Comparator.comparing(ShortEntityDTO::getName))
                .collect(Collectors.toList());
    }

    /**
     * Creates project with OWNER's permissions for current user.
     *
     * @param projectDTO Project to create
     * @param user       creator of the project
     * @return Created project
     */
    public ProjectDTO createProject(ProjectDTO projectDTO, User user) {
        Project project = mapper.convertFromDTO(projectDTO);

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userService, project.getAccessList());

        PermissionChanges<Project> permissions = ProjectPermissionHelper.fillNewProjectPermissions(project, user);

        project.setId(sequenceIdService.getNextProjectId());

        project = saveProjectAndHandleError(project);

        final Set<String> fileIds = project.getFileIds();
        final List<GridFsResource> temporaryFiles = fileRepository.findTemporary(fileIds);
        temporaryFiles.forEach(tf -> {
            GridFSFileUtil.setTemporaryToMetadata(tf.getOptions().getMetadata(), false);
            fileRepository.updateMetadata(tf, tf.getOptions().getMetadata());
        });

        webSocketUtil.newProject(user, getSubEntityChangesRecipients(permissions, userService.getContentEditors())
                .filter(userId -> !userId.equals(user.getId())));

        return new ProjectDTO(project);
    }

    /**
     * Updates project according to permissions.
     *
     * @param projectDTO Project to update
     * @param user       User
     * @return Updated project
     */
    public ProjectDTO updateProject(ProjectDTO projectDTO, User user) {
        Project projectFromDb = projectRepository.findById(projectDTO.getId())
                .orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectDTO.getId()));

        // check of EntityAccess (User must have "Update Entity" permission in project's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, projectFromDb.getAccessList(),
                UserPermission.UPDATE_ENTITY)) {
            throw OperationDeniedException.createProjectUpdateOperation(projectFromDb.getId());
        }

        Project project = mapper.convertFromDTO(projectDTO);
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userService, project.getAccessList());

        // do not change old project's notebooks and file ids and author
        projectFromDb.setName(project.getName());
        projectFromDb.setDescription(project.getDescription());
        projectFromDb.setTags(project.getTags());
        projectFromDb.setKeywords(project.getKeywords());
        projectFromDb.setReferences(project.getReferences());
        projectFromDb.setVersion(project.getVersion());

        Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
                changes = ProjectPermissionHelper.changeProjectPermissions(projectFromDb, project.getAccessList(), user);

        Project savedProject = saveProjectAndHandleError(projectFromDb);

        Set<User> contentEditors = userService.getContentEditors();

        List<WebSocketUtil.DelayedNotificationRunnable> notifications = new ArrayList<>(
                getProjectNotifications(user, savedProject, changes.getLeft(), contentEditors));

        if (changes.getLeft().hadChanged()) {

            notifications.addAll(
                    updateNotebooksAndGetNotifications(user, projectFromDb, changes.getRight().keySet(), contentEditors));


            notifications.addAll(
                    updateExperimentsAndGetNotifications(user, projectFromDb,
                            changes.getRight().entrySet().stream()
                                    .map(e -> Pair.of(e.getKey(), e.getValue()))
                                    .collect(Collectors.toList()), contentEditors));
        }

        webSocketUtil.sendAll(notifications);

        return new ProjectDTO(savedProject);
    }

    private List<WebSocketUtil.DelayedNotificationRunnable> getProjectNotifications(
            User user,
            Project projectFromDb,
            PermissionChanges<Project> projectPermissionChanges,
            Set<User> contentEditors
    ) {
        WebSocketUtil.DelayedNotificationRunnable newProjectNotification = webSocketUtil.getNewProjectNotification(user,
                getSubEntityChangesRecipients(projectPermissionChanges));

        Stream<String> recipients =
                getEntityUpdateRecipients(contentEditors, projectFromDb, user.getId())
                        .distinct();

        WebSocketUtil.DelayedNotificationRunnable updateProjectNotification =
                webSocketUtil.getUpdateProjectNotification(user, projectFromDb, recipients);

        return asList(newProjectNotification, updateProjectNotification);
    }

    private List<WebSocketUtil.DelayedNotificationRunnable> updateNotebooksAndGetNotifications(
            User user,
            Project projectFromDb,
            Set<PermissionChanges<Notebook>> permissionChanges,
            Set<User> contentEditors
    ) {
        List<WebSocketUtil.DelayedNotificationRunnable> result = new ArrayList<>();

        Set<PermissionChanges<Notebook>> notebooksChanges = permissionChanges
                .stream()
                .filter(PermissionChanges::hadChanged)
                .collect(toSet());


        notebookRepository.saveAll(notebooksChanges.stream()
                .map(PermissionChanges::getEntity)
                .collect(toSet()));

        result.add(
                webSocketUtil.getProjectSubEntityNotification(user, projectFromDb,
                        getSubEntityChangesRecipients(notebooksChanges)));

        result.addAll(notebooksChanges.stream()
                .map(notebookPermissionChanges -> webSocketUtil
                        .getUpdateNotebookNotification(user, projectFromDb.getId(),
                                notebookPermissionChanges.getEntity(), getEntityUpdateRecipients(contentEditors,
                                        notebookPermissionChanges.getEntity(), null)))
                .collect(toList()));

        return result;

    }

    private List<WebSocketUtil.DelayedNotificationRunnable> updateExperimentsAndGetNotifications(
            User user,
            Project projectFromDb,
            List<Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>> changes,
            Set<User> contentEditors
    ) {
        List<WebSocketUtil.DelayedNotificationRunnable> result = new ArrayList<>();

        for (Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> change : changes) {
            if (change.getKey().hadChanged()) {
                List<PermissionChanges<Experiment>> experimentsChanges = change.getValue()
                        .stream()
                        .filter(PermissionChanges::hadChanged)
                        .collect(Collectors.toList());
                experimentRepository.saveAll(experimentsChanges.stream()
                        .map(PermissionChanges::getEntity)
                        .collect(Collectors.toList()));
                result.add(
                        webSocketUtil.getNotebookSubEntityNotification(user,
                                projectFromDb.getId(), change.getKey().getEntity(),
                                getSubEntityChangesRecipients(experimentsChanges)));
                result.addAll(
                        experimentsChanges.stream()
                                .map(experimentPermissionChanges ->
                                        webSocketUtil.getUpdateExperimentNotification(user, projectFromDb.getId(),
                                                change.getKey().getEntity().getId(),
                                                experimentPermissionChanges.getEntity(),
                                                getEntityUpdateRecipients(contentEditors,
                                                        experimentPermissionChanges.getEntity(),
                                                        null)))
                                .collect(toList()));
            }
        }
        return result;
    }


    /**
     * Removes project.
     *
     * @param id Project's identifier
     */
    public void deleteProject(String id) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        Project project = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(id));

        if (project.getNotebooks() != null && !project.getNotebooks().isEmpty()) {
            throw new ChildReferenceException(project.getId());
        }

        fileRepository.delete(project.getFileIds());
        projectRepository.delete(project);
    }

    /**
     * Checks if user can be deleted from project's access list with all the permissions without any problems.
     * It checks all the notebooks and experiments and if any has this user added, then it will return false.
     *
     * @param projectId project id
     * @param userId    user id
     * @return true if none of notebooks or experiments has user added to it, true otherwise
     */
    public boolean isUserRemovable(String projectId, String userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        Project project = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        List<Notebook> notebooks = project.getNotebooks();
        boolean addedToNotebooks = notebooks.stream().anyMatch(n -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(n.getAccessList(), userId);
            return permission != null;
        });
        if (addedToNotebooks) {
            return false;
        }

        List<Experiment> experiments = notebooks.stream().flatMap(n -> n.getExperiments().stream())
                .collect(Collectors.toList());
        return experiments.stream().noneMatch(e -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(e.getAccessList(), userId);
            return permission != null;
        });

    }

    private Project saveProjectAndHandleError(Project project) {
        try {
            return projectRepository.save(project);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithProjectName(project.getName(), e);
        } catch (OptimisticLockingFailureException e) {
            throw ConcurrencyException.createWithProjectName(project.getName(), e);
        }
    }

    public void deleteAll() {
        projectRepository.deleteAll();
    }

    public Stream<Triple<Project, Notebook, Experiment>> getEntities(Map<String, Experiment> experiments) {
        List<DBRef> experimentIds = experiments.keySet().stream()
                .map(k -> new DBRef("experiment", k))
                .collect(Collectors.toList());

        Map<String, Notebook> notebooks = notebookRepository.findByExperimentsIds(experimentIds)
                .collect(Collectors.toMap(Notebook::getId, Function.identity()));

        List<DBRef> notebookIds = notebooks.keySet().stream()
                .map(k -> new DBRef("notebook", k))
                .collect(Collectors.toList());

        Collection<Project> projects = projectRepository.findByNotebookIds(notebookIds);

        return projects.parallelStream()
                .flatMap(p -> p.getNotebooks().stream()
                        .filter(n -> notebooks.get(n.getId()) != null)
                        .flatMap(n -> n.getExperiments().stream()
                                .filter(e -> experiments.get(e.getId()) != null)
                                .map(e -> Triple.of(p, n, e))));
    }
}
