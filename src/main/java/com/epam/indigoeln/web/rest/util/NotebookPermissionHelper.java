package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.UserPermission.VIEWER_PERMISSIONS;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.findPermissionsByUserId;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.hasUser;
import static java.util.stream.Collectors.toSet;

public class NotebookPermissionHelper {

    private NotebookPermissionHelper() {
    }

    public static void addPermissionsFromUpperLevel(Notebook notebook, Set<UserPermission> userPermissions) {

        notebook.getAccessList().addAll(userPermissions);
        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.addPermissionsFromUpperLevel(experiment, userPermissions));
    }

    public static void updatePermissionFromUpperLevel(Notebook notebook, Set<UserPermission> updatedPermissions) {

        notebook.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        notebook.getAccessList().addAll(updatedPermissions);

        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.updatePermissionFromUpperLevel(experiment, updatedPermissions));
    }

    public static void removePermissionFromUpperLevel(Notebook notebook, Set<UserPermission> removedPermissions) {
        notebook.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.removePermissionsFromUpperLevel(experiment, removedPermissions));
    }

    public static boolean addPermissions(Project project, Notebook notebook, Set<UserPermission> addedPermissions) {

        notebook.getAccessList().addAll(addedPermissions);
        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.addPermissionsFromUpperLevel(experiment, addedPermissions));

        return ProjectPermissionHelper.addPermissionsFromLowerLevel(project, addedPermissions);
    }

    public static void updatePermissions(Notebook notebook, Set<UserPermission> updatedPermissions) {

        updatePermissionFromUpperLevel(notebook, updatedPermissions);
    }

    public static boolean removePermissions(Project project, Notebook notebook, Set<UserPermission> removedPermission) {

        removePermissionFromUpperLevel(notebook, removedPermission);

        Set<UserPermission> canBeRemovedFromProject = removedPermission.stream()
                .filter(permission ->
                        !permission.getPermissionCreationLevel().equals(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        return ProjectPermissionHelper.removePermissionFromLowerLevel(project, notebook, canBeRemovedFromProject);
    }

    public static Pair<Boolean, Boolean> addPermissionsFromLowerLevel(Project project,
                                                                      Notebook notebook,
                                                                      Set<UserPermission> addedToExperimentPermissions
    ) {
        HashSet<UserPermission> addedToNotebook = new HashSet<>();

        for (UserPermission addedPermission : addedToExperimentPermissions) {
            UserPermission presentedPermission =
                    findPermissionsByUserId(notebook.getAccessList(), addedPermission.getUser().getId());
            if (presentedPermission == null) {
                UserPermission addingPermission = new UserPermission(addedPermission.getUser(), VIEWER_PERMISSIONS,
                        addedPermission.getPermissionCreationLevel());
                notebook.getAccessList().add(addingPermission);
                addedToNotebook.add(addingPermission);
            }
        }

        boolean projectWasUpdated = ProjectPermissionHelper.addPermissionsFromLowerLevel(project, addedToNotebook);

        return Pair.of(projectWasUpdated, !addedToNotebook.isEmpty());
    }

    public static Pair<Boolean, Boolean> removePermissionFromLowerLevel(Project project,
                                                                        Notebook notebook,
                                                                        Experiment fromExperiment,
                                                                        Set<UserPermission> removedPermissions
    ) {

        Set<UserPermission> presentedInOtherNotebooks = notebook.getExperiments().stream()
                .filter(experiment -> !experiment.equals(fromExperiment))
                .flatMap(experiment -> experiment.getAccessList().stream())
                .collect(toSet());

        Set<UserPermission> canBeRemovedFromNotebook =
                removedPermissions.stream()
                        .filter(removedPermission ->
                                !hasUser(presentedInOtherNotebooks, removedPermission))
                        .collect(toSet());

        boolean projectWasUpdated = removePermissions(project, notebook, canBeRemovedFromNotebook);

        return Pair.of(projectWasUpdated, !canBeRemovedFromNotebook.isEmpty());
    }
}
