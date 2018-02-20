package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.exception.*;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.epam.indigoeln.web.rest.util.permission.helpers.NotebookPermissionHelper;
import com.epam.indigoeln.web.rest.util.permission.helpers.PermissionChanges;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.util.WebSocketUtil.getEntityUpdateRecipients;
import static com.epam.indigoeln.core.util.WebSocketUtil.getSubEntityChangesRecipients;
import static java.util.Collections.singletonList;

/**
 * Provides a number of methods for notebook's data manipulation.
 */
@Service
public class NotebookService {

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

    /**
     * Instance of UserService for access to user in database.
     */
    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private SequenceIdService sequenceIdService;

    /**
     * Instance of CustomerDtoMapper for conversion from DTO object.
     */
    @Autowired
    private CustomDtoMapper dtoMapper;

    @Autowired
    private WebSocketUtil webSocketUtil;

    /**
     * Instance of ExperimentService class for working with experiment's data.
     */
    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Returns all notebooks of specified project for tree representation.
     *
     * @param projectId Project's identifier
     * @return List with notebooks for tree representation
     */
    public List<TreeNodeDTO> getAllNotebookTreeNodes(String projectId) {
        return getAllNotebookTreeNodes(projectId, null);
    }

    /**
     * Returns all notebooks of specified project for current user.
     * for tree representation according to his permissions.
     *
     * @param projectId Project's identifier
     * @param user      User
     * @return List with notebooks for tree representation
     */
    @SuppressWarnings("unchecked")
    public List<TreeNodeDTO> getAllNotebookTreeNodes(String projectId, User user) {
        val projectCollection = mongoTemplate.getCollection(Project.COLLECTION_NAME);
        val notebookCollection = mongoTemplate.getCollection(Notebook.COLLECTION_NAME);

        val project = projectCollection.findOne(new BasicDBObject().append("_id", projectId));
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }

        if (project.get("notebooks") instanceof Iterable) {
            val notebookIds = new ArrayList<String>();
            ((Iterable) project.get("notebooks")).forEach(n -> notebookIds.add(String.valueOf(((DBRef) n).getId())));

            val notebooks = new ArrayList<DBObject>();
            notebookCollection.find(new BasicDBObject("_id", new BasicDBObject("$in", notebookIds)))
                    .forEach(notebooks::add);

            if (user == null) {
                return notebooks.stream()
                        .map(TreeNodeDTO::new)
                        .sorted(TreeNodeDTO.NAME_COMPARATOR)
                        .collect(Collectors.toList());
            }

            val projectAccessList = new HashSet<UserPermission>();
            ((Iterable) project.get("accessList")).forEach(a -> projectAccessList.add(new UserPermission((DBObject) a)));

            if (!PermissionUtil.hasPermissions(user.getId(), projectAccessList, UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createProjectSubEntitiesReadOperation(String.valueOf(project.get("_id")));
            }

            return notebooks.stream()
                    .filter(notebook -> {
                        val notebookAccessList = new HashSet<UserPermission>();
                        ((Iterable) notebook.get("accessList"))
                                .forEach(a -> notebookAccessList.add(new UserPermission((DBObject) a)));
                        return PermissionUtil.findPermissionsByUserId(notebookAccessList, user.getId()) != null;
                    })
                    .map(TreeNodeDTO::new)
                    .sorted(TreeNodeDTO.NAME_COMPARATOR)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Fetch all notebooks for current user available when create sub entity
     * return notebooks where users is in accessList and has permission CREATE_SUB_ENTITY,
     * or has authority CONTENT_EDITOR (but not in accessLIst).
     *
     * @param user user
     * @return list of notebooks available for experiment creation
     */
    public List<ShortEntityDTO> getNotebooksForExperimentCreation(User user) {
        List<Notebook> notebooks = PermissionUtil.isContentEditor(user)
                ? notebookRepository.findAllIgnoreChildren()
                : notebookRepository.findByUserIdAndPermissionsShort(user.getId(),
                singletonList(UserPermission.CREATE_SUB_ENTITY));
        return notebooks.stream().map(ShortEntityDTO::new)
                .sorted(Comparator.comparing(ShortEntityDTO::getName)).collect(Collectors.toList());
    }

    /**
     * Returns notebook by id according to permissions.
     *
     * @param projectId Project's identifier
     * @param id        Notebook's identifier
     * @param user      User
     * @return Notebook by id
     */
    public NotebookDTO getNotebookById(String projectId, String id, User user) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, id);
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(id));

        // Check of EntityAccess (User must have "Read Entity" permission in project access list and
        // "Read Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(fullNotebookId);
            if (project == null) {
                throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_ENTITY,
                    notebook.getAccessList(), UserPermission.READ_ENTITY)) {
                throw OperationDeniedException.createNotebookReadOperation(notebook.getId());
            }
        }

        return new NotebookDTO(notebook);
    }

    public TreeNodeDTO getNotebookAsTreeNode(String projectId, String notebookId){
        TreeNodeDTO result;
        val notebookCollection = mongoTemplate.getCollection(Notebook.COLLECTION_NAME);
        val notebook = notebookCollection.findOne(new BasicDBObject().append("_id", projectId+"-"+notebookId));
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }
        result = new TreeNodeDTO(notebook);

        return result;
    }

    /**
     * Creates notebook with OWNER's permissions for current user.
     *
     * @param notebookDTO Notebook to create
     * @param projectId   Project's identifier
     * @param user        User
     * @return Created notebook
     */
    public NotebookDTO createNotebook(NotebookDTO notebookDTO, String projectId, User user) {
        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntityCreateOperation(project.getId());
        }

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);

        // reset notebook's id
        notebook.setId(sequenceIdService.getNextNotebookId(projectId));

        // check of user permissions correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userService, notebook.getAccessList());
        // add OWNER's permissions for specified User to notebook
        Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> changes =
                NotebookPermissionHelper.fillNewNotebooksPermissions(project, notebook, user);

        Notebook savedNotebook = saveNotebookAndHandleError(notebook);

        project.getNotebooks().add(savedNotebook);
        projectRepository.save(project);

        webSocketUtil.newProject(user, getSubEntityChangesRecipients(changes.getLeft()));
        webSocketUtil.newSubEntityForProject(user, project, getSubEntityChangesRecipients(changes.getMiddle()));

        return new NotebookDTO(savedNotebook);
    }

    /**
     * Updates notebook with OWNER's permissions for current user.
     *
     * @param notebookDTO Notebook to update
     * @param projectId   Project's identifier
     * @param user        User
     * @return Updated notebook
     */
    public NotebookDTO updateNotebook(NotebookDTO notebookDTO, String projectId, User user) {
        Lock lock = experimentService.getLock(projectId);
        try {
            lock.lock();
            String fullNotebookId = SequenceIdUtil.buildFullId(projectId, notebookDTO.getId());
            Notebook notebookFromDB = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                    orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookDTO.getId()));

            // Check of EntityAccess (User must have "Read Entity" permission in project access list and
            // "Update Entity" permission in notebook access list, or must have CONTENT_EDITOR authority)

            Project project = projectRepository.findByNotebookId(fullNotebookId);
            if (project == null) {
                throw EntityNotFoundException.createWithNotebookChildId(notebookFromDB.getId());
            }

            if (!PermissionUtil.isContentEditor(user) && !PermissionUtil.hasPermissions(user.getId(),
                    project.getAccessList(), UserPermission.READ_ENTITY,
                    notebookFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw OperationDeniedException.createNotebookUpdateOperation(notebookFromDB.getId());
            }

            Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);
            // check of user permissions's correctness in access control list
            PermissionUtil.checkCorrectnessOfAccessList(userService, notebook.getAccessList());

            boolean notebookNameChanged = !notebookFromDB.getName().equals(notebook.getName());
            if (notebookNameChanged) {
                List<String> numbers = BatchComponentUtil.hasBatches(notebookFromDB);
                if (!numbers.isEmpty()) {
                    throw OperationDeniedException
                            .createNotebookUpdateNameOperation();
                }
                boolean hasNotOpen = notebookFromDB.getExperiments().stream()
                        .anyMatch(e -> e.getStatus() != ExperimentStatus.OPEN);
                if (hasNotOpen) {
                    throw OperationDeniedException
                            .createNotebookUpdateNameOperation();
                }
                notebookFromDB.setName(notebook.getName());
                notebookFromDB.getExperiments().forEach(e -> e.compileExperimentFullName(notebook.getName()));
            }
            notebookFromDB.setDescription(notebook.getDescription());
            notebookFromDB.setVersion(notebook.getVersion());

            Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
                    permissionsChanges = NotebookPermissionHelper
                    .changeNotebookPermissions(project, notebookFromDB, notebook.getAccessList(), user);

            Set<User> contentEditors = userService.getContentEditors();

            PermissionChanges<Notebook> notebooksChanges = permissionsChanges.getMiddle();
            if (notebooksChanges.hadChanged() || notebookNameChanged) {
                updateProjectAndSendNotifications(user, permissionsChanges.getLeft(), contentEditors);

                updateExperimentsAndSendNotifications(user, project.getId(), contentEditors,
                        notebooksChanges, permissionsChanges.getRight(), notebookNameChanged);
            }
            sendNotebookNotifications(user, project, notebookFromDB,
                    contentEditors, notebooksChanges);

            Notebook savedNotebook = saveNotebookAndHandleError(notebookFromDB);

            return new NotebookDTO(savedNotebook);
        } finally {
            lock.unlock();
        }
    }

    private void sendNotebookNotifications(User user,
                                           Project project,
                                           Notebook notebook,
                                           Set<User> contentEditors,
                                           PermissionChanges<Notebook> notebooksChanges
    ) {
        webSocketUtil.newSubEntityForProject(user, project,
                getSubEntityChangesRecipients(notebooksChanges));

        Stream<String> recipients =
                getEntityUpdateRecipients(contentEditors, notebook, user.getId())
                        .distinct();

        webSocketUtil.updateNotebook(user, project.getId(), notebook,
                recipients);
    }

    private void updateProjectAndSendNotifications(User user,
                                                   PermissionChanges<Project> permissionChanges,
                                                   Set<User> contentEditors
    ) {
        if (permissionChanges.hadChanged()) {
            Project savedProject = projectRepository.save(permissionChanges.getEntity());

            webSocketUtil.updateProject(user, savedProject,
                    getEntityUpdateRecipients(
                            contentEditors, permissionChanges.getEntity(), user.getId()));

            webSocketUtil.newProject(user, getSubEntityChangesRecipients(permissionChanges));
        }
    }

    private void updateExperimentsAndSendNotifications(
            User user,
            String projectId,
            Set<User> contentEditors,
            PermissionChanges<Notebook> notebookPermissionChanges, List<PermissionChanges<Experiment>> allExperimentsChanges,
            boolean notebookNameChanged
    ) {
        List<PermissionChanges<Experiment>> experimentsChanges = allExperimentsChanges
                .stream()
                .filter(PermissionChanges::hadChanged)
                .collect(Collectors.toList());

        Notebook parentNotebook = notebookPermissionChanges.getEntity();
        if (notebookNameChanged) {
            //we need to re-save all experiments if name of the notebook was changed
            List<Experiment> savedExperiments = experimentRepository.save(
                    notebookPermissionChanges.getEntity().getExperiments());
            savedExperiments.forEach(experiment ->
                    webSocketUtil.updateExperiment(user, projectId, parentNotebook.getId(), experiment,
                            getEntityUpdateRecipients(contentEditors, experiment, user.getId())));
        } else if (notebookPermissionChanges.hadChanged()) {
            experimentRepository.save(experimentsChanges.stream()
                    .map(PermissionChanges::getEntity)
                    .collect(Collectors.toList()));

            experimentsChanges.forEach(experimentPermissionChanges ->
                    webSocketUtil.updateExperiment(
                            user,
                            projectId,
                            parentNotebook.getId(),
                            experimentPermissionChanges.getEntity(),
                            getEntityUpdateRecipients(contentEditors,
                                    experimentPermissionChanges.getEntity(),
                                    user.getId())));
        }
        webSocketUtil.newSubEntityForNotebook(user, projectId, parentNotebook,
                getSubEntityChangesRecipients(experimentsChanges));
    }

    /**
     * Removes notebook.
     *
     * @param projectId Project's identifier
     * @param id        Notebook's identifier
     */
    public void deleteNotebook(String projectId, String id) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, id);
        Notebook notebook = Optional.ofNullable(notebookRepository.findOne(fullNotebookId)).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(id));

        if (notebook.getExperiments() != null && !notebook.getExperiments().isEmpty()) {
            throw new ChildReferenceException(notebook.getId());
        }

        Project project = projectRepository.findByNotebookId(fullNotebookId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
        }

        project.getNotebooks().remove(notebook);
        projectRepository.save(project);

        notebookRepository.delete(notebook);
    }

    /**
     * Checks if user can be deleted from notebook's access list with all the permissions without any problems.
     * It checks all the experiments and if any has this user added, then it will return false.
     *
     * @param projectId  project id
     * @param notebookId notebook id
     * @param userId     user id
     * @return true if none of experiments has user added to it, true otherwise
     */
    public boolean isUserRemovable(String projectId, String notebookId, String userId) {
        String fullNotebookId = SequenceIdUtil.buildFullId(projectId, notebookId);
        Optional<Notebook> notebookOpt = Optional.ofNullable(notebookRepository.findOne(fullNotebookId));
        Notebook notebook = notebookOpt.orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

        return notebook.getExperiments().stream().noneMatch(e -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(e.getAccessList(), userId);
            return permission != null;
        });
    }

    /**
     * Checks if notebook name is new or not
     *
     * @param notebookName Notebook name to check
     * @return true if name is new and false if not
     */
    public boolean isNew(String notebookName) {
        return !notebookRepository.findByName(notebookName).isPresent();
    }

    private Notebook saveNotebookAndHandleError(Notebook notebook) {
        try {
            return notebookRepository.save(notebook);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithNotebookName(notebook.getName(), e);
        } catch (OptimisticLockingFailureException e) {
            throw ConcurrencyException.createWithNotebookName(notebook.getName(), e);
        }
    }

    public void deleteAll() {
        notebookRepository.deleteAll();
    }
}
