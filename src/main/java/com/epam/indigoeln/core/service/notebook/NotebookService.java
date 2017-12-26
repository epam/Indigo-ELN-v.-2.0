package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.*;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
     * Instance of UserRepository for access to user in database.
     */
    @Autowired
    private UserRepository userRepository;

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

    private static List<Notebook> getNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        return notebooks.stream().filter(notebook -> PermissionUtil.findFirstPermissionsByUserId(
                notebook.getAccessList(), userId) != null).collect(Collectors.toList());
    }

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
    public List<TreeNodeDTO> getAllNotebookTreeNodes(String projectId, User user) {
        Collection<Notebook> notebooks = getAllNotebooks(projectId, user);
        return notebooks.stream().map(TreeNodeDTO::new).sorted(TreeNodeDTO.NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve notebooks without checking for UserPermissions.
     * Otherwise, use checking for UserPermissions
     *
     * @param projectId Project's identifier
     * @param user      User
     * @return All notebooks
     */
    private Collection<Notebook> getAllNotebooks(String projectId, User user) {
        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        if (user == null) {
            return project.getNotebooks();
        }

        // Check of EntityAccess (User must have "Read Entity" permission in project access list)
        if (!PermissionUtil.hasPermissions(user.getId(), project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createProjectSubEntitiesReadOperation(project.getId());
        }

        return getNotebooksWithAccess(project.getNotebooks(), user.getId());

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
                Collections.singletonList(UserPermission.CREATE_SUB_ENTITY));
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
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebook.getAccessList());
        // add OWNER's permissions for specified User to notebook
        PermissionUtil.addOwnerToAccessList(notebook.getAccessList(), user);
        //add permissions to notebook and project
        Notebook notebookWithPermissions = new Notebook();
        PermissionUtil.changeNotebookPermissions(project, notebookWithPermissions,
                PermissionUtil.addFirstEntityName(notebook.getAccessList(), FirstEntityName.NOTEBOOK));

        PermissionUtil.addUsersFromUpperLevel(
                notebookWithPermissions.getAccessList(), project.getAccessList(), FirstEntityName.PROJECT);

        notebook.setAccessList(notebookWithPermissions.getAccessList());

        Notebook savedNotebook = saveNotebookAndHandleError(notebook);

        project.getNotebooks().add(savedNotebook);
        Project savedProject = projectRepository.save(project);
        webSocketUtil.updateProject(user, savedProject);

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
            PermissionUtil.checkCorrectnessOfAccessList(userRepository, notebook.getAccessList());

            if (!notebookFromDB.getName().equals(notebookDTO.getName())) {
                List<String> numbers = BatchComponentUtil.hasBatches(notebookFromDB);
                if (!numbers.isEmpty()) {
                    throw OperationDeniedException
                            .createNotebookUpdateNameOperation(numbers.toArray(new String[numbers.size()]));
                }
                notebookFromDB.setName(notebookDTO.getName());
            }
            notebookFromDB.setDescription(notebookDTO.getDescription());
            notebookFromDB.setVersion(notebook.getVersion());

            boolean projectWasUpdated = PermissionUtil.changeNotebookPermissions(
                    project, notebookFromDB,
                    PermissionUtil.addFirstEntityName(notebook.getAccessList(), FirstEntityName.NOTEBOOK));

            experimentRepository.save(notebookFromDB.getExperiments());

            Notebook savedNotebook = saveNotebookAndHandleError(notebookFromDB);

            webSocketUtil.updateNotebook(user, projectId, savedNotebook);

            if (projectWasUpdated) {
                Project savedProject = projectRepository.save(project);
                webSocketUtil.updateProject(user, savedProject);
            }

            return new NotebookDTO(savedNotebook);
        } finally {
            lock.unlock();
        }
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
            UserPermission permission = PermissionUtil.findFirstPermissionsByUserId(e.getAccessList(), userId);
            return permission != null;
        });
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
}