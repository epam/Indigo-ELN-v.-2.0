package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.exception.ChildReferenceException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

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
        return projects.stream().
                map(project -> new TreeNodeDTO(new ProjectDTO(project), hasNotebooks(project, user))).
                collect(Collectors.toList());
    }

    /**
     * If user is null, then check only project's notebooks list for empty
     * Otherwise, use checking for UserPermissions
     */
    private boolean hasNotebooks(Project project, User user) {
        if (user == null) {
            return !project.getNotebooks().isEmpty();
        }
        // Checking of userPermission for "Read Sub-Entity" possibility,
        // and that project has notebooks with UserPermission for specified User
        return PermissionUtil.hasPermissions(user.getId(), project.getAccessList(),
                UserPermission.READ_SUB_ENTITY) &&
                hasNotebooksWithAccess(project.getNotebooks(), user.getId());
    }

    private boolean hasNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        // Because we have one at least Notebook with UserPermission for Read Entity
        return notebooks != null &&
                notebooks.stream().anyMatch(notebook -> PermissionUtil.findPermissionsByUserId(notebook.getAccessList(), userId) != null);

    }

    public ProjectDTO getProjectById(String id, User user) {
        Optional<Project> projectOpt = Optional.ofNullable(projectRepository.findOne(id));
        Project project = projectOpt.orElseThrow(() -> EntityNotFoundException.createWithProjectId(id));

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw OperationDeniedException.createProjectReadOperation(project.getId());
        }
        return new ProjectDTO(project);
    }

    public ProjectDTO createProject(ProjectDTO projectDTO, User user) {
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, projectDTO.getAccessList());
        // add OWNER's permissions to project
        PermissionUtil.addOwnerToAccessList(projectDTO.getAccessList(), user);
        // reset project's id
        Project projectForSave = mapper.convertFromDTO(projectDTO);
        projectForSave.setId(sequenceIdService.getNextProjectId());

        Project saved = projectRepository.save(projectForSave);
        return new ProjectDTO(saved);
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

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, projectDTO.getAccessList());

        // do not change old project's notebooks and file ids and author
        projectFromDb.setName(projectDTO.getName());
        projectFromDb.setDescription(projectDTO.getDescription());
        projectFromDb.setTags(projectDTO.getTags());
        projectFromDb.setKeywords(projectDTO.getKeywords());
        projectFromDb.setReferences(projectDTO.getReferences());
        projectFromDb.setAccessList(projectDTO.getAccessList());

        Project savedProject = projectRepository.save(projectFromDb);
        return new ProjectDTO(savedProject);
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
}