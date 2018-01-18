package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

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
}
