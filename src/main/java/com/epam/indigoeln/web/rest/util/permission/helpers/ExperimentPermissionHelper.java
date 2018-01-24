package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.EXPERIMENT;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
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

    /**
     * @return {@code true} if some permissions were added to experiment's access list
     */
    static boolean addPermissionsFromNotebook(Experiment experiment, Set<UserPermission> userPermissions) {

        boolean experimentWasChanged = false;
        for (UserPermission userPermission : userPermissions) {
            if (userPermission.getPermissionView().equals(UserPermission.USER)) {
                experimentWasChanged |= experiment.getAccessList().add(new UserPermission(userPermission.getUser(),
                        UserPermission.VIEWER_PERMISSIONS, userPermission.getPermissionCreationLevel()));
            } else {
                experimentWasChanged |= experiment.getAccessList().add(userPermission);
            }
        }
        return experimentWasChanged;
    }

    /**
     * Cascade update experiment permission after updates in notebooks permissions.
     * <p>
     * If permission for user was set on experiment level it could be changed only from experiment.
     *
     * @param experiment         to change accessList
     * @param updatedPermissions updated permissions
     * @return {@code true} if some permissions were updated to experiment's access list
     */
    static boolean updatePermissionFromNotebook(Experiment experiment,
                                                Set<UserPermission> updatedPermissions
    ) {
        boolean experimentWasChanged = false;
        for (UserPermission updatedPermission : updatedPermissions) {
            UserPermission presentedPermission =
                    findPermissionsByUserId(experiment.getAccessList(), updatedPermission.getUser().getId());

            if (presentedPermission == null) {

                experiment.getAccessList().add(updatedPermission);
                experimentWasChanged |= true;

            } else if (updatedPermission.getPermissionCreationLevel().equals(EXPERIMENT)
                    || !presentedPermission.getPermissionCreationLevel().equals(EXPERIMENT)) {
                if (updatedPermission.getPermissionView().equals(UserPermission.USER)) {
                    updatedPermission = new UserPermission(updatedPermission.getUser(),
                            UserPermission.VIEWER_PERMISSIONS, updatedPermission.getPermissionCreationLevel());
                }
                if (!presentedPermission.getPermissionView().equals(updatedPermission.getPermissionView())) {
                    experiment.getAccessList().remove(presentedPermission);
                    experiment.getAccessList().add(updatedPermission);
                    experimentWasChanged |= true;
                }
            }
        }
        return experimentWasChanged;
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

        Set<UserPermission> createdPermissions =
                getCreatedPermission(updatedExperiment, newUserPermissions, EXPERIMENT);

        if (!createdPermissions.isEmpty()) {
            Pair<Boolean, Boolean> projectAndNotebookHadChanged =
                    addPermissions(project, notebook, updatedExperiment, createdPermissions);

            projectHadChanged = projectAndNotebookHadChanged.getLeft();
            notebookHadChanged = projectAndNotebookHadChanged.getRight();
        }

        Set<UserPermission> updatedPermissions =
                getUpdatedPermissions(updatedExperiment, newUserPermissions, EXPERIMENT);

        if (!updatedPermissions.isEmpty()) {
            updatePermission(updatedExperiment, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(updatedExperiment, newUserPermissions);

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
