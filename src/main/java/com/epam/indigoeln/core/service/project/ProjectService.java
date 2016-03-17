package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.ChildReferenceException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomDtoMapper mapper;

    @Autowired
    private SequenceIdService sequenceIdService;

    public List<TreeNodeDTO> getAllProjectsAsTreeNodes() {
        return getAllProjectsAsTreeNodes(null);
    }

    /**
     * If user is null, then retrieve projects without checking for UserPermissions
     * Otherwise, use checking for UserPermissions
     */
    public List<TreeNodeDTO> getAllProjectsAsTreeNodes(User user) {
        // if user is null, then get all projects
        Collection<Project> projects = user == null ? projectRepository.findAll() :
                projectRepository.findByUserId(user.getId());
        return projects.stream().map(TreeNodeDTO::new).sorted().collect(Collectors.toList());
    }

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
     * or has authority CONTENT_EDITOR (but not in accessLIst)
     * @param user user
     * @return list of projects available for notebook creation
     */
    public List<ShortEntityDTO> getProjectsForNotebookCreation(User user) {
        List<Project> projects = PermissionUtil.isContentEditor(user) ?
                projectRepository.findAllIgnoreChildren() :
                projectRepository.findByUserIdAndPermissions(user.getId(), Collections.singletonList(UserPermission.CREATE_SUB_ENTITY));

        return projects.stream().map(ShortEntityDTO::new).sorted(Comparator.comparing(ShortEntityDTO::getName)).collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO projectDTO, User user) {
        Project project = mapper.convertFromDTO(projectDTO);

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, project.getAccessList());
        // reset project's id
        project.setId(sequenceIdService.getNextProjectId());

        project = saveProjectAndHandleError(project);
        return new ProjectDTO(project);
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO, User user) {
        Optional<Project> projectOpt =  Optional.ofNullable(projectRepository.findOne(projectDTO.getId()));
        Project projectFromDb = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectDTO.getId()));

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
        projectFromDb.setAccessList(project.getAccessList());

        project = saveProjectAndHandleError(projectFromDb);
        return new ProjectDTO(project);
    }

    public void deleteProject(String id) {
        Optional<Project> projectOpt =  Optional.ofNullable(projectRepository.findOne(id));
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
     * @param projectId project id
     * @param userId user id
     * @return true if none of notebooks or experiments has user added to it, true otherwise
     */
    public boolean isUserRemovable(String projectId, String userId) {
        Optional<Project> projectOpt =  Optional.ofNullable(projectRepository.findOne(projectId));
        Project project = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));

        List<Notebook> notebooks = project.getNotebooks();
        boolean addedToNotebooks = notebooks.stream().filter(n -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(n.getAccessList(), userId);
            return permission != null;
        }).count() > 0;
        if (addedToNotebooks) {
            return false;
        }

        List<Experiment> experiments = notebooks.stream().flatMap(n -> n.getExperiments().stream())
                .collect(Collectors.toList());
        return experiments.stream().filter(e -> {
            UserPermission permission = PermissionUtil.findPermissionsByUserId(e.getAccessList(), userId);
            return permission != null;
        }).count() == 0;

    }

    private Project saveProjectAndHandleError(Project project) {
        try {
            return projectRepository.save(project);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithProjectName(project.getName());
        }
    }
}