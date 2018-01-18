package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.UserPermission.VIEWER_PERMISSIONS;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.findPermissionsByUserId;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.hasUser;
import static java.util.stream.Collectors.toSet;

public class NotebookPermissionHelper {

    private NotebookPermissionHelper() {
    }

    public static boolean addPermissions(Project project, Notebook notebook, Set<UserPermission> addedPermissions) {

        notebook.getAccessList().addAll(addedPermissions);
        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.addPermissionsFromNotebook(experiment, addedPermissions));

        return ProjectPermissionHelper.addPermissionsFromNotebook(project, addedPermissions);
    }

    public static void updatePermissions(Notebook notebook, Set<UserPermission> updatedPermissions) {

        updatePermissionFromProject(notebook, updatedPermissions);
    }

    public static boolean removePermissions(Project project, Notebook notebook, Set<UserPermission> removedPermission) {

        removePermissionFromProject(notebook, removedPermission);

        Set<UserPermission> canBeRemovedFromProject = removedPermission.stream()
                .filter(permission ->
                        !permission.getPermissionCreationLevel().equals(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        return ProjectPermissionHelper.removePermissionFromNotebook(project, notebook, canBeRemovedFromProject);
    }


    static void addPermissionsFromProject(Notebook notebook, Set<UserPermission> userPermissions) {

        notebook.getAccessList().addAll(userPermissions);
        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.addPermissionsFromNotebook(experiment, userPermissions));
    }

    static void updatePermissionFromProject(Notebook notebook, Set<UserPermission> updatedPermissions) {

        notebook.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        notebook.getAccessList().addAll(updatedPermissions);

        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.updatePermissionFromNotebook(experiment, updatedPermissions));
    }

    static void removePermissionFromProject(Notebook notebook, Set<UserPermission> removedPermissions) {
        notebook.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.removePermissionsFromNotebook(experiment, removedPermissions));
    }

    public static Pair<Boolean, Boolean> addPermissionsFromExperiment(Project project,
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

        boolean projectWasUpdated = ProjectPermissionHelper.addPermissionsFromNotebook(project, addedToNotebook);

        return Pair.of(projectWasUpdated, !addedToNotebook.isEmpty());
    }

    public static Pair<Boolean, Boolean> removePermissionFromExperiment(Project project,
                                                                        Notebook notebook,
                                                                        Experiment fromExperiment,
                                                                        Set<UserPermission> removedPermissions
    ) {

        Set<UserPermission> presentedInOtherNotebooks = notebook.getExperiments().stream()
                .filter(experiment -> !experiment.getId().equals(fromExperiment.getId()))
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

    /**
     * Change notebooks access list according to {@code newUserPermissions}.
     * <p>
     * This method calls changing of project permissions as well and returns boolean flags
     * that mean was project changed or not.
     *
     * @param project            Project that contains {@code notebook}
     * @param notebook           Notebook to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return {@code true} if project's access list was changed.
     */
    public static boolean changeNotebookPermissions(Project project,
                                                    Notebook notebook,
                                                    Set<UserPermission> newUserPermissions
    ) {
        boolean projectHadChanged = false;

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                PermissionUtil.equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {

            projectHadChanged = addPermissions(project, notebook, createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream().anyMatch(oldPermission ->
                        PermissionUtil.equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {

            updatePermissions(notebook, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = notebook.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> PermissionUtil.equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            projectHadChanged |= removePermissions(project, notebook, removedPermissions);
        }
        return projectHadChanged;
    }

    public static void fillNewNotebooksPermissions(Project project, Notebook notebook, User creator) {
        Set<UserPermission> accessList = notebook.getAccessList();
        PermissionUtil.addUsersFromUpperLevel(
                accessList, project.getAccessList(), PermissionCreationLevel.PROJECT);
        PermissionUtil.addOwnerToAccessList(accessList, creator, PermissionCreationLevel.NOTEBOOK);

        notebook.setAccessList(new HashSet<>());
        notebook.setExperiments(Collections.emptyList());
        changeNotebookPermissions(project, notebook, accessList);
    }
}
