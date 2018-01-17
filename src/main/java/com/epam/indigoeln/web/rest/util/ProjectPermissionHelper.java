package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.UserPermission;

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
                NotebookPermissionHelper.addPermissionsFromUpperLevel(notebook, createdPermissions));
    }

    public static void updatePermissions(Project project, Set<UserPermission> updatedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        project.getAccessList().addAll(updatedPermissions);

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.updatePermissionFromUpperLevel(notebook, updatedPermissions));
    }

    public static void removePermission(Project project, Set<UserPermission> removedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.removePermissionFromUpperLevel(notebook, removedPermissions));
    }

    public static boolean addPermissionsFromLowerLevel(Project project, Set<UserPermission> addedPermissions) {

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

    public static boolean removePermissionFromLowerLevel(Project project, Notebook fromNotebook, Set<UserPermission> removedFromNotebook) {

        Set<UserPermission> presentedInOtherNotebooks = project.getNotebooks().stream()
                .filter(notebook -> !notebook.equals(fromNotebook))
                .flatMap(notebook -> notebook.getAccessList().stream())
                .collect(toSet());

        Set<UserPermission> canBeRemovedFromProject =
                removedFromNotebook.stream()
                        .filter(removedPermission ->
                                !hasUser(presentedInOtherNotebooks, removedPermission)).collect(toSet());

        return project.getAccessList().removeIf(userPermission -> hasUser(canBeRemovedFromProject, userPermission));
    }
}
