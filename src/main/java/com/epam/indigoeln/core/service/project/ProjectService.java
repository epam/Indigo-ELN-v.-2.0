package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.ChildReferenceException;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.epam.indigoeln.web.rest.util.PermissionUtil.hasPermissions;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FileRepository fileRepository;

    public Collection<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Collection<Project> getAllProjects(User user) {
        return PermissionUtil.isContentEditor(user) ?
                projectRepository.findAll() : projectRepository.findByUserId(user.getId());
    }

    public Project getProjectById(String projectId, User user) {
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project's access list,
        // or must have CONTENT_EDITOR authority)
        if (!hasPermissions(user, project.getAccessList(),
                UserPermission.READ_ENTITY)) {
            throw new AccessDeniedException(
                    "The current user doesn't have permissions to read project with id = " + projectId);
        }
        return project;
    }

    public Project getProjectByName(String name) {
        return projectRepository.findByName(name);
    }

    public Project createProject(Project project, User user) {
        // reset project's id
        project.setId(null);
        // Adding of author and OWNER's permissions to project
        project.setAuthor(user);
        PermissionUtil.addOwnerToAccessList(project.getAccessList(), user.getId());
        return projectRepository.save(project);
    }

    public Project updateProject(Project project, User user) {
        Project projectFromDb = projectRepository.findOne(project.getId());
        if (projectFromDb == null) {
            throw EntityNotFoundException.createWithProjectId(project.getId());
        }

        // Check of EntityAccess (User must have "Update Entity" permission in project's access list,
        // or must have CONTENT_EDITOR authority)
        if (!hasPermissions(user, projectFromDb.getAccessList(),
                UserPermission.UPDATE_ENTITY)) {
            throw new AccessDeniedException(
                    "The current user doesn't have permissions to edit project with id = " + project.getId());
        }
        // Set old project's notebooks and file ids to new project
        project.setNotebooks(projectFromDb.getNotebooks());
        project.setFileIds(projectFromDb.getFileIds());

        return projectRepository.save(project);
    }

    public void deleteProject(String id) {
        Project project = projectRepository.findOne(id);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(id);
        }

        if (!project.getNotebooks().isEmpty()) {
            throw new ChildReferenceException(project.getId());
        }

        fileRepository.delete(project.getFileIds());
        projectRepository.delete(project);
    }
}