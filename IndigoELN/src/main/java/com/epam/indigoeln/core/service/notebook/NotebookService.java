package com.epam.indigoeln.core.service.notebook;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.ChildReferenceException;
import com.epam.indigoeln.core.service.EntityAlreadyExistsException;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotebookService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    public Collection<Notebook> getAllNotebooks(String projectId, User user) {
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project access list,
        // or must have CONTENT_EDITOR authority)
        if (PermissionUtil.isContentEditor(user)) {
            return project.getNotebooks();
        } else if (PermissionUtil.hasPermissions(user, project.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            return getNotebooksWithAccess(project.getNotebooks(), user.getId());
        }
        throw new AccessDeniedException("The current user doesn't have permissions to read " +
                "notebooks of project with id = " + project.getId());
    }

    public Notebook getNotebookById(String id, User user) {
        Notebook notebook = notebookRepository.findOne(id);
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(id);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in project access list and
        // "Read Entity" permission in notebook access list, or must have <b>ADMIN</b> authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(id);
            if (project == null) {
                throw EntityNotFoundException.createWithProjectChildId(id);
            }

            if (!PermissionUtil.hasPermissions(user,
                    project.getAccessList(), UserPermission.READ_SUB_ENTITY,
                    notebook.getAccessList(), UserPermission.READ_ENTITY)) {
                throw new AccessDeniedException("The current user doesn't have permissions " +
                        "to read notebook with id = " + notebook.getId());
            }
        }
        return notebook;
    }

    public Notebook createNotebook(Notebook notebook, String projectId, User user) {
        Project project = projectRepository.findOne(projectId);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectId(projectId);
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasPermissions(user, project.getAccessList(),
                    UserPermission.CREATE_SUB_ENTITY)) {
            throw new AccessDeniedException(
                    "The current user doesn't have permissions to create notebook");
        }

        // check for duplication of name
        if (notebookRepository.findByName(notebook.getName()) != null) {
            throw EntityAlreadyExistsException.createWithNotebookName(notebook.getName());
        }

        // reset notebook's id
        notebook.setId(null);
        // Adding of OWNER's permissions for specified User to notebook
        PermissionUtil.addOwnerToAccessList(notebook.getAccessList(), user.getId());
        notebook = notebookRepository.save(notebook);

        project.getNotebooks().add(notebook);
        projectRepository.save(project);
        return notebook;
    }

    public Notebook updateNotebook(Notebook notebook, User user) {
        Notebook notebookFromDB = notebookRepository.findOne(notebook.getId());
        if (notebookFromDB == null) {
            throw EntityNotFoundException.createWithNotebookId(notebook.getId());
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in project access list and
        // "Update Entity" permission in notebook access list, or must have <b>ADMIN</b> authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Project project = projectRepository.findByNotebookId(notebook.getId());
            if (project == null) {
                throw EntityNotFoundException.createWithNotebookChildId(notebook.getId());
            }

            if (!PermissionUtil.hasPermissions(user,
                    project.getAccessList(), UserPermission.CREATE_SUB_ENTITY,
                    notebookFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw new AccessDeniedException(
                        "The current user doesn't have permissions to update notebook with id = " + notebook.getId());
            }
        }

        // check for duplication of name
        Notebook notebookByName = notebookRepository.findByName(notebook.getName());
        if (notebookByName != null && !notebookByName.getId().equals(notebook.getId())) {
            throw EntityAlreadyExistsException.createWithNotebookName(notebook.getName());
        }

        // Set old notebook's experiment ids to new notebook
        notebook.setExperiments(notebookFromDB.getExperiments());
        return notebookRepository.save(notebook);
    }

    public void deleteNotebook(String id) {
        Notebook notebook = notebookRepository.findOne(id);
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(id);
        }

        if(!notebook.getExperiments().isEmpty()) {
            throw new ChildReferenceException(notebook.getId());
        }

        Project project = projectRepository.findByNotebookId(notebook.getId());
        if (project == null) {
            throw EntityNotFoundException.createWithProjectChildId(notebook.getId());
        }

        project.getNotebooks().remove(notebook);
        projectRepository.save(project);

        notebookRepository.delete(notebook);
    }

    public boolean hasNotebooks(Project project, User user) {
        boolean isAdmin = PermissionUtil.isContentEditor(user);
        if (isAdmin) {
            return !project.getNotebooks().isEmpty();
        } else {
            UserPermission userPermission = PermissionUtil.findPermissionsByUserId(
                    project.getAccessList(), user.getId());
            // Checking of userPermission for "Read Sub-Entity" possibility,
            // and that project has notebooks with UserPermission for specified User
            return userPermission != null && userPermission.canReadSubEntity()
                    && hasNotebooksWithAccess(project.getNotebooks(), user.getId());
        }
    }

    private static boolean hasNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        for (Notebook notebook : notebooks) {
            if (PermissionUtil.findPermissionsByUserId(notebook.getAccessList(), userId) != null) {
                // Because we have one at least Notebook with UserPermission for Read Entity
                return true;
            }
        }
        return false;
    }

    private static List<Notebook> getNotebooksWithAccess(List<Notebook> notebooks, String userId) {
        return notebooks.stream().filter(notebook -> PermissionUtil.findPermissionsByUserId(
                notebook.getAccessList(), userId) != null).collect(Collectors.toList());
    }
}