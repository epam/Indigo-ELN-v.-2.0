package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.EXPERIMENT;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.hasUser;
import static java.util.stream.Collectors.toSet;

public class ExperimentPermissionHelper {

    private ExperimentPermissionHelper() {
    }

    public static Pair<Boolean, Boolean> addPermissions(Project project,
                                                        Notebook notebook,
                                                        Experiment experiment,
                                                        Set<UserPermission> addedPermissions
    ) {

        experiment.getAccessList().addAll(addedPermissions);

        return NotebookPermissionHelper.addPermissionsFromExperiment(project, notebook, addedPermissions);
    }

    /**
     * Update permissions in experiment.
     * <p>
     * If permissions are updated that mean that ones already presented in {@link Project} and {@link Notebook}
     * as VIEWER_PERMISSIONS. So there is no need to update notebooks or projects permissions.
     * Old permissions from experiment's accessList for users that present in updatedPermissions
     * will be replaced by updatedPermissions.
     *
     * @param experiment         entity to permission updates
     * @param updatedPermissions permissions witch should be updated
     */
    public static void updatePermission(Experiment experiment, Set<UserPermission> updatedPermissions) {
        updatePermissionFromNotebook(experiment, updatedPermissions);
    }

    public static Pair<Boolean, Boolean> removePermissions(Project project,
                                                           Notebook notebook,
                                                           Experiment experiment,
                                                           Set<UserPermission> removedPermissions
    ) {

        removePermissionsFromNotebook(experiment, removedPermissions);

        Set<UserPermission> permissionsCreatedFromThisExperiment =
                removedPermissions.stream()
                        .filter(removedPermission ->
                                removedPermission.getPermissionCreationLevel()
                                        .equals(PermissionCreationLevel.EXPERIMENT))
                        .collect(toSet());

        return NotebookPermissionHelper.removePermissionFromExperiment(project, notebook,
                experiment, permissionsCreatedFromThisExperiment);
    }

    static void addPermissionsFromNotebook(Experiment experiment, Set<UserPermission> userPermissions) {
        for (UserPermission userPermission : userPermissions) {
            if (userPermission.getPermissionView().equals(UserPermission.USER)) {
                experiment.getAccessList().add(new UserPermission(userPermission.getUser(),
                        UserPermission.VIEWER_PERMISSIONS, userPermission.getPermissionCreationLevel()));
            } else {
                experiment.getAccessList().add(userPermission);
            }
        }
    }

    static void updatePermissionFromNotebook(Experiment experiment, Set<UserPermission> updatedPermissions) {
        removePermissionsFromNotebook(experiment, updatedPermissions);
        addPermissionsFromNotebook(experiment, updatedPermissions);
    }

    static boolean removePermissionsFromNotebook(Experiment experiment,
                                                 Set<UserPermission> updatedPermissions
    ) {
        return experiment.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
    }

    /**
     * Change experiments access list according to {@code newUserPermissions}.
     * <p>
     * This method calls changing of notebook and project permissions as well
     * and returns the pair of two boolean flags these mean (notebook was changed, project was changed).
     *
     * @param project            Project that contains {@code notebook}
     * @param notebook           Notebook that contains changing {@code updatedExperiment}
     * @param updatedExperiment  Experiment to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return pair of two boolean flags these mean (notebook was changed, project was changed).
     */
    public static Pair<Boolean, Boolean> changeExperimentPermissions(Project project,
                                                                     Notebook notebook,
                                                                     Experiment updatedExperiment,
                                                                     Set<UserPermission> newUserPermissions
    ) {
        boolean notebookHadChanged = false;
        boolean projectHadChanged = false;

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> updatedExperiment.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                PermissionUtil.equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(EXPERIMENT))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            Pair<Boolean, Boolean> projectAndNotebookHadChanged =
                    addPermissions(project, notebook, updatedExperiment, createdPermissions);

            projectHadChanged = projectAndNotebookHadChanged.getLeft();
            notebookHadChanged = projectAndNotebookHadChanged.getRight();
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> updatedExperiment.getAccessList().stream().anyMatch(oldPermission ->
                        PermissionUtil.equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(EXPERIMENT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            updatePermission(updatedExperiment, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = updatedExperiment.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> PermissionUtil.equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            Pair<Boolean, Boolean> projectAndNotebookHadChanged =
                    removePermissions(
                            project, notebook, updatedExperiment, removedPermissions);

            projectHadChanged |= projectAndNotebookHadChanged.getLeft();
            notebookHadChanged |= projectAndNotebookHadChanged.getRight();
        }

        return Pair.of(notebookHadChanged, projectHadChanged);
    }

    public static Pair<Boolean, Boolean> fillNewExperimentsPermissions(Project project,
                                                                       Notebook notebook,
                                                                       Experiment experiment,
                                                                       User creator
    ) {

        Set<UserPermission> permissions = experiment.getAccessList();
        experiment.setAccessList(new HashSet<>());
        PermissionUtil.addUsersFromUpperLevel(permissions, notebook.getAccessList(),
                PermissionCreationLevel.NOTEBOOK);

        PermissionUtil.addOwnerToAccessList(permissions, creator,
                PermissionCreationLevel.EXPERIMENT);

        return changeExperimentPermissions(project, notebook, experiment, permissions);
    }
}
