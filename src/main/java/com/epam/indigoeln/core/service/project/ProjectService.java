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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.gridfs.GridFSDBFile;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epam.indigoeln.core.util.WebSocketUtil.getEntityUpdateRecipients;
import static com.epam.indigoeln.core.util.WebSocketUtil.getSubEntityChangesRecipients;
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
                : collection.find(new BasicDBObject()
                .append("accessList.user", new BasicDBObject()
                .append("$ref", User.COLLECTION_NAME).append("$id", user.getId())));

        return StreamSupport.stream(projects.spliterator(), false)
                .map(TreeNodeDTO::new)
                .sorted(TreeNodeDTO.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    public TreeNodeDTO getProjectAsTreeNode(String projectId){
        TreeNodeDTO result;
        val collection = mongoTemplate.getCollection(Project.COLLECTION_NAME);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", projectId);
        val project = collection.find(searchQuery);
        if(project.hasNext()){
            result = new TreeNodeDTO(project.next());
        }else {
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
        Optional<Project> projectOpt = Optional.ofNullable(projectRepository.findOne(id));
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

        val projects = new HashMap<Object, DBObject>();

        if (PermissionUtil.isContentEditor(user)) {
            projectCollection
                    .find()
                    .forEach(p -> projects.put(p.get("_id"), p));
        } else {
            projectCollection
                    .find(new BasicDBObject()
                            .append("accessList", new BasicDBObject()
                                    .append("$elemMatch", new BasicDBObject()
                                            .append("user.$id", user.getId())
                                            .append("permissions", new BasicDBObject()
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

        val users = new HashMap<Object, DBObject>();
        userCollection
                .find(new BasicDBObject("_id", new BasicDBObject("$in", userIds)))
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
        final List<GridFSDBFile> temporaryFiles = fileRepository.findTemporary(fileIds);
        temporaryFiles.forEach(tf -> {
            GridFSFileUtil.setTemporaryToMetadata(tf.getMetaData(), false);
            tf.save();
        });

        webSocketUtil.newProject(user, getSubEntityChangesRecipients(permissions)
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
        Project projectFromDb = Optional.ofNullable(projectRepository.findOne(projectDTO.getId()))
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

        Set<User> contentEditors = userService.getContentEditors();
        sendProjectNotifications(user, projectFromDb, changes.getLeft(), contentEditors);
        if (changes.getLeft().hadChanged()) {

            updateNotebooksAndSendNotifications(user, projectFromDb, changes.getRight().keySet(), contentEditors);
            updateExperimentsAndSendNotifications(user, projectFromDb,
                    changes.getRight().entrySet().stream()
                            .map(e -> Pair.of(e.getKey(), e.getValue()))
                            .collect(Collectors.toList()), contentEditors);
        }

        Project savedProject = saveProjectAndHandleError(projectFromDb);

        return new ProjectDTO(savedProject);
    }

    private void sendProjectNotifications(User user,
                                          Project projectFromDb,
                                          PermissionChanges<Project> projectPermissionChanges,
                                          Set<User> contentEditors
    ) {
        webSocketUtil.newProject(user, getSubEntityChangesRecipients(projectPermissionChanges));
        Stream<String> recipients =
                getEntityUpdateRecipients(contentEditors, projectFromDb, user.getId())
                        .distinct();
        webSocketUtil.updateProject(user, projectFromDb, recipients);
    }

    private void updateNotebooksAndSendNotifications(User user,
                                                     Project projectFromDb,
                                                     Set<PermissionChanges<Notebook>> permissionChanges,
                                                     Set<User> contentEditors
    ) {
        Set<PermissionChanges<Notebook>> notebooksChanges = permissionChanges
                .stream()
                .filter(PermissionChanges::hadChanged)
                .collect(toSet());

        notebookRepository.save(notebooksChanges.stream()
                .map(PermissionChanges::getEntity)
                .collect(toSet()));

        webSocketUtil.newSubEntityForProject(user, projectFromDb,
                getSubEntityChangesRecipients(notebooksChanges));

        notebooksChanges.forEach(notebookPermissionChanges ->
                webSocketUtil.updateNotebook(user, projectFromDb.getId(),
                        notebookPermissionChanges.getEntity(),
                        getEntityUpdateRecipients(contentEditors,
                                notebookPermissionChanges.getEntity(),
                                user.getId())));
    }

    private void updateExperimentsAndSendNotifications(
            User user,
            Project projectFromDb,
            List<Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>> changes,
            Set<User> contentEditors
    ) {
        for (Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> change : changes) {
            if (change.getKey().hadChanged()) {
                List<PermissionChanges<Experiment>> experimentsChanges = change.getValue()
                        .stream()
                        .filter(PermissionChanges::hadChanged)
                        .collect(Collectors.toList());
                experimentRepository.save(experimentsChanges.stream()
                        .map(PermissionChanges::getEntity)
                        .collect(Collectors.toList()));
                webSocketUtil.newSubEntityForNotebook(user, projectFromDb.getId(), change.getKey().getEntity(),
                        getSubEntityChangesRecipients(experimentsChanges));

                experimentsChanges.forEach(experimentPermissionChanges ->
                        webSocketUtil.updateExperiment(user, projectFromDb.getId(),
                                change.getKey().getEntity().getId(),
                                experimentPermissionChanges.getEntity(),
                                getEntityUpdateRecipients(contentEditors,
                                        experimentPermissionChanges.getEntity(),
                                        user.getId())));
            }
        }
    }


    /**
     * Removes project.
     *
     * @param id Project's identifier
     */
    public void deleteProject(String id) {
        Optional<Project> projectOpt = Optional.ofNullable(projectRepository.findOne(id));
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
        Optional<Project> projectOpt = Optional.ofNullable(projectRepository.findOne(projectId));
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
