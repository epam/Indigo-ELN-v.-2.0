package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.EXPERIMENT;
import static com.epam.indigoeln.core.model.PermissionCreationLevel.NOTEBOOK;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
import static java.util.stream.Collectors.toSet;

public class ExperimentPermissionHelper {

    private ExperimentPermissionHelper() {
    }

    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>>
    addPermissions(Project project,
                   Notebook notebook,
                   Experiment experiment,
                   Set<UserPermission> addedPermissions
    ) {
        Set<UserPermission> addedToExperimentPermissions = addAllIfNotPresent(experiment, addedPermissions);

        PermissionChanges<Experiment> experimentPermissionChanges =
                PermissionChanges.ofCreatedPermissions(experiment, addedToExperimentPermissions);

        Pair<PermissionChanges<Project>, PermissionChanges<Notebook>> projectAndNotebookPermissionChanges =
                NotebookPermissionHelper.addPermissionsFromExperiment(project, notebook, addedToExperimentPermissions);

        return Triple.of(
                projectAndNotebookPermissionChanges.getLeft(),
                projectAndNotebookPermissionChanges.getRight(),
                experimentPermissionChanges);
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
    public static PermissionChanges<Experiment> updatePermission(Experiment experiment,
                                                                 Set<UserPermission> updatedPermissions
    ) {
        return updatePermissionFromNotebook(experiment, updatedPermissions);
    }

    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>>
    removePermissions(Project project,
                      Notebook notebook,
                      Experiment experiment,
                      Set<UserPermission> removedPermissions
    ) {

        PermissionChanges<Experiment> experimentPermissionChanges =
                removePermissionsFromNotebook(experiment, removedPermissions);

        Set<UserPermission> permissionsCreatedFromThisExperiment =
                removedPermissions.stream()
                        .filter(removedPermission ->
                                removedPermission.getPermissionCreationLevel()
                                        .equals(EXPERIMENT))
                        .collect(toSet());

        Pair<PermissionChanges<Project>, PermissionChanges<Notebook>> projectAndNotebookChanges =
                NotebookPermissionHelper.removePermissionFromExperiment(
                        project, notebook, experiment, permissionsCreatedFromThisExperiment);

        return Triple.of(projectAndNotebookChanges.getLeft(), projectAndNotebookChanges.getRight(), experimentPermissionChanges);
    }

    /**
     * @return {@code true} if some permissions were added to experiment's access list
     */
    static PermissionChanges<Experiment> addPermissionsFromNotebook(Experiment experiment, Set<UserPermission> userPermissions) {

        Set<UserPermission> addedPermissions = new HashSet<>();

        for (UserPermission userPermission : userPermissions) {
            if (userPermission.getPermissionView().equals(UserPermission.USER)) {
                UserPermission addingPermission = new UserPermission(userPermission.getUser(),
                        UserPermission.VIEWER_PERMISSIONS, userPermission.getPermissionCreationLevel());

                experiment.getAccessList().add(addingPermission);

                addedPermissions.add(addingPermission);
            } else {
                experiment.getAccessList().add(userPermission);

                addedPermissions.add(userPermission);
            }
        }
        return PermissionChanges.ofCreatedPermissions(experiment, addedPermissions);
    }

    /**
     * Cascade update experiment permission after updates in notebooks permissions.
     * <p>
     * If permission for user was set on experiment level it could be changed only from experiment.
     *
     * @param experiment         to change accessList
     * @param updatedPermissions updated permissions
     * @return permission changes
     */
    static PermissionChanges<Experiment> updatePermissionFromNotebook(Experiment experiment,
                                                                      Set<UserPermission> updatedPermissions
    ) {
        Set<UserPermission> newPermissions = new HashSet<>();
        Set<UserPermission> changedPermissions = new HashSet<>();

        for (UserPermission updatedPermission : updatedPermissions) {
            UserPermission presentedPermission =
                    findPermissionsByUserId(experiment.getAccessList(), updatedPermission.getUser().getId());

            if (updatedPermission.getPermissionView().equals(UserPermission.USER)) {
                updatedPermission = new UserPermission(updatedPermission.getUser(),
                        UserPermission.VIEWER_PERMISSIONS, updatedPermission.getPermissionCreationLevel(),
                        updatedPermission.isRemovable());
            }
            if (presentedPermission == null) {

                experiment.getAccessList().add(updatedPermission);
                newPermissions.add(updatedPermission);

            } else if ((updatedPermission.getPermissionCreationLevel().equals(EXPERIMENT)
                    || !presentedPermission.getPermissionCreationLevel().equals(EXPERIMENT))
                    && !presentedPermission.getPermissionView().equals(updatedPermission.getPermissionView())) {
                UserPermission changedPermission =
                        presentedPermission.setPermissions(updatedPermission.getPermissions());
                changedPermissions.add(changedPermission);
            }
        }
        return new PermissionChanges<>(experiment, newPermissions, changedPermissions);
    }

    static PermissionChanges<Experiment> removePermissionsFromNotebook(Experiment experiment,
                                                                       Set<UserPermission> updatedPermissions
    ) {
        Set<UserPermission> removedPermissions = experiment.getAccessList().stream()
                .filter(userPermission -> hasUser(updatedPermissions, userPermission))
                .collect(toSet());

        experiment.getAccessList().removeAll(removedPermissions);

        return PermissionChanges.ofRemovedPermissions(experiment, removedPermissions);
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
    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>>
    changeExperimentPermissions(Project project,
                                Notebook notebook,
                                Experiment updatedExperiment,
                                Set<UserPermission> newUserPermissions
    ) {

        PermissionChanges<Experiment> experimentPermissionChanges = new PermissionChanges<>(updatedExperiment);
        PermissionChanges<Notebook> notebookPermissionChanges = new PermissionChanges<>(notebook);
        PermissionChanges<Project> projectPermissionChanges = new PermissionChanges<>(project);

        Set<UserPermission> createdPermissions =
                getCreatedPermission(updatedExperiment, newUserPermissions, EXPERIMENT);

        if (!createdPermissions.isEmpty()) {

            Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>> changes =
                    addPermissions(project, notebook, updatedExperiment, createdPermissions);

            projectPermissionChanges.merge(changes.getLeft());
            notebookPermissionChanges.merge(changes.getMiddle());
            experimentPermissionChanges.merge(changes.getRight());
        }

        Set<UserPermission> updatedPermissions =
                getUpdatedPermissions(updatedExperiment, newUserPermissions, EXPERIMENT);

        if (!updatedPermissions.isEmpty()) {
            PermissionChanges<Experiment> updatedExperimentPermissions = updatePermission(updatedExperiment, updatedPermissions);

            experimentPermissionChanges.merge(updatedExperimentPermissions);
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(updatedExperiment, newUserPermissions);

        if (!removedPermissions.isEmpty()) {

            Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>> changes =
                    removePermissions(
                            project, notebook, updatedExperiment, removedPermissions);

            projectPermissionChanges.merge(changes.getLeft());
            notebookPermissionChanges.merge(changes.getMiddle());
            experimentPermissionChanges.merge(changes.getRight());
        }

        return Triple.of(projectPermissionChanges, notebookPermissionChanges, experimentPermissionChanges);
    }

    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, PermissionChanges<Experiment>>
    fillNewExperimentsPermissions(Project project,
                                  Notebook notebook,
                                  Experiment experiment,
                                  User creator
    ) {

        Set<UserPermission> permissions = experiment.getAccessList();
        experiment.setAccessList(new HashSet<>());
        PermissionUtil.addOwnerToAccessList(experiment.getAccessList(), creator, EXPERIMENT);

        PermissionUtil.addUsersFromUpperLevel(permissions, notebook.getAccessList(), NOTEBOOK);

        updatePermission(experiment, getUpdatedPermissions(experiment, permissions, EXPERIMENT));
        return addPermissions(project, notebook, experiment, getCreatedPermission(experiment, permissions, EXPERIMENT));
    }
}
