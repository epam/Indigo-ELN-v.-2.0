package com.epam.indigoeln.core.service.project;

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
        return projects.stream().map(ProjectService::convertToTreeNode).sorted().collect(Collectors.toList());
    }

    private static TreeNodeDTO convertToTreeNode(Project project) {
        TreeNodeDTO result = new TreeNodeDTO(project);
        if(project.getNotebooks() != null) {
            result.setChildren(project.getNotebooks().stream().
                    map(notebook -> new TreeNodeDTO(notebook, notebook.getExperiments())).
                    sorted().collect(Collectors.toList()));
        }
        return result;
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
        Project project = mapper.convertFromDTO(projectDTO);

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, project.getAccessList());
        // reset project's id
        project.setId(sequenceIdService.getNextProjectId());

        project = projectRepository.save(project);
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

        project = projectRepository.save(projectFromDb);
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
}