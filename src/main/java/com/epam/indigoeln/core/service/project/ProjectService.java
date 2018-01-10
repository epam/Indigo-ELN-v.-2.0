package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.file.GridFSFileUtil;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.*;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
     * Instance of UserRepository for access to users in database.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Instance of CustomDtoMapper for conversion from dto object.
     */
    @Autowired
    private CustomDtoMapper mapper;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private WebSocketUtil webSocketUtil;

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
        Collection<Project> projects = user == null ? projectRepository.findAll()
                : projectRepository.findByUserId(user.getId());
        return projects.stream().map(TreeNodeDTO::new).sorted(TreeNodeDTO.NAME_COMPARATOR)
                .collect(Collectors.toList());
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
        List<Project> projects = PermissionUtil.isContentEditor(user)
                ? projectRepository.findAllIgnoreChildren()
                : projectRepository.findByUserIdAndPermissions(user.getId(),
                Collections.singletonList(UserPermission.CREATE_SUB_ENTITY));

        return projects.stream().map(ShortEntityDTO::new)
                .sorted(Comparator.comparing(ShortEntityDTO::getName)).collect(Collectors.toList());
    }

    /**
     * Creates project with OWNER's permissions for current user.
     *
     * @param projectDTO Project to create
     * @return Created project
     */
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = mapper.convertFromDTO(projectDTO);

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, project.getAccessList());

        //Add entity name
        project.getAccessList().forEach(userPermission ->
                userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT));
        project.setId(sequenceIdService.getNextProjectId());

        project = saveProjectAndHandleError(project);

        final Set<String> fileIds = project.getFileIds();
        final List<GridFSDBFile> temporaryFiles = fileRepository.findTemporary(fileIds);
        temporaryFiles.forEach(tf -> {
            GridFSFileUtil.setTemporaryToMetadata(tf.getMetaData(), false);
            tf.save();
        });

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
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, project.getAccessList());

        // do not change old project's notebooks and file ids and author
        projectFromDb.setName(project.getName());
        projectFromDb.setDescription(project.getDescription());
        projectFromDb.setTags(project.getTags());
        projectFromDb.setKeywords(project.getKeywords());
        projectFromDb.setReferences(project.getReferences());
        projectFromDb.setVersion(project.getVersion());

        PermissionUtil.changeProjectPermissions(projectFromDb, project.getAccessList());

        List<Notebook> notebooks = projectFromDb.getNotebooks();
        notebooks.forEach(notebook -> experimentRepository.save(notebook.getExperiments()));
        notebookRepository.save(notebooks);

        projectFromDb.setNotebooks(notebooks);
        project = saveProjectAndHandleError(projectFromDb);
        webSocketUtil.updateProject(user, project);
        return new ProjectDTO(project);
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
}
