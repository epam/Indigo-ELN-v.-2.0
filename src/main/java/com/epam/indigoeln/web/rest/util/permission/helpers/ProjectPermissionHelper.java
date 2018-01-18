package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.PermissionCreationLevel;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.web.rest.util.PermissionUtil;

import java.util.Set;

import static com.epam.indigoeln.core.model.UserPermission.VIEWER_PERMISSIONS;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.findPermissionsByUserId;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.hasUser;
import static java.util.stream.Collectors.toSet;

public class ProjectPermissionHelper {

    private ProjectPermissionHelper() {
    }

    public static void addPermissions(Project project, Set<UserPermission> createdPermissions) {
        project.getAccessList().addAll(createdPermissions);

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.addPermissionsFromProject(notebook, createdPermissions));
    }

    public static void updatePermissions(Project project, Set<UserPermission> updatedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        project.getAccessList().addAll(updatedPermissions);

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.updatePermissionFromProject(notebook, updatedPermissions));
    }

    public static void removePermission(Project project, Set<UserPermission> removedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.removePermissionFromProject(notebook, removedPermissions));
    }

    static boolean addPermissionsFromNotebook(Project project, Set<UserPermission> addedPermissions) {

        boolean result = false;

        for (UserPermission addedPermission : addedPermissions) {
            UserPermission presentedPermission =
                    findPermissionsByUserId(project.getAccessList(), addedPermission.getUser().getId());
            if (presentedPermission == null) {
                project.getAccessList().add(
                        new UserPermission(addedPermission.getUser(), VIEWER_PERMISSIONS,
                                addedPermission.getPermissionCreationLevel()));
                result = true;
            }
        }
        return result;
    }

    static boolean removePermissionFromNotebook(Project project,
                                                Notebook fromNotebook,
                                                Set<UserPermission> removedFromNotebook
    ) {

        Set<UserPermission> presentedInOtherNotebooks = project.getNotebooks().stream()
                .filter(notebook -> !notebook.getId().equals(fromNotebook.getId()))
                .flatMap(notebook -> notebook.getAccessList().stream())
                .collect(toSet());

        Set<UserPermission> canBeRemovedFromProject =
                removedFromNotebook.stream()
                        .filter(removedPermission ->
                                !hasUser(presentedInOtherNotebooks, removedPermission)).collect(toSet());

        return project.getAccessList().removeIf(userPermission -> hasUser(canBeRemovedFromProject, userPermission));
    }

    /**
     * Change projects access list according to {@code newUserPermissions}.
     *
     * @param project            project to change permissions
     * @param newUserPermissions permissions that should be applied
     */
    public static void changeProjectPermissions(Project project, Set<UserPermission> newUserPermissions) {

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                PermissionUtil.equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {

            addPermissions(project, createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        PermissionUtil.equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {

            updatePermissions(project, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> PermissionUtil.equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            removePermission(project, removedPermissions);
        }
    }
}
