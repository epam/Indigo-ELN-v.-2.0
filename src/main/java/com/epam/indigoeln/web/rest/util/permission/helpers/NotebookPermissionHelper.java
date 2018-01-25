package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.NOTEBOOK;
import static com.epam.indigoeln.core.model.UserPermission.VIEWER_PERMISSIONS;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class NotebookPermissionHelper {

    private NotebookPermissionHelper() {
    }

    /**
     * Add permissions no notebook, its project and all experiments.
     * <p>
     * Adding notebooks permission always add permission for all of notebooks experiment
     * and might add permission as viewer to the project if it isn't already added by other notebooks.
     *
     * @param project          project that contains notebook
     * @param notebook         an modifying notebook
     * @param addedPermissions permissions that should be added
     * @return {@code true} if project's access list was changed.
     * and {@code false} if project's access list was changed.
     */
    public static boolean addPermissions(Project project, Notebook notebook, Set<UserPermission> addedPermissions) {

        notebook.getAccessList().addAll(addedPermissions);
        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.addPermissionsFromNotebook(experiment, addedPermissions));

        return ProjectPermissionHelper.addPermissionsFromNotebook(project, addedPermissions);
    }

    public static Pair<Notebook, List<Experiment>> updatePermissions(Notebook notebook, Set<UserPermission> updatedPermissions) {

        return updatePermissionFromProject(notebook, updatedPermissions);
    }

    public static boolean removePermissions(Project project, Notebook notebook, Set<UserPermission> removedPermission) {

        removePermissionFromProject(notebook, removedPermission);

        Set<UserPermission> canBeRemovedFromProject = removedPermission.stream()
                .filter(permission ->
                        !permission.getPermissionCreationLevel().equals(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        return ProjectPermissionHelper.removePermissionFromNotebook(project, notebook, canBeRemovedFromProject);
    }


    static boolean addPermissionsFromProject(Notebook notebook, Set<UserPermission> userPermissions) {

        if (notebook.getAccessList().addAll(userPermissions)) {
            notebook.getExperiments().forEach(experiment ->
                    ExperimentPermissionHelper.addPermissionsFromNotebook(experiment, userPermissions));
            return true;
        }
        return false;
    }

    /**
     * Cascade update notebooks permissions after project permissions update.
     * <p>
     * If notebooks permission for user was set from notebook it can only be changed from notebook.
     *
     * @param notebook           notebook to update access list
     * @param updatedPermissions permission that should be updated
     * @return pair of updated notebook and experiments that were updated in this notebook
     * {@code null} if notebook wasn't update.
     */
    @Nullable
    static Pair<Notebook, List<Experiment>> updatePermissionFromProject(Notebook notebook,
                                                                        Set<UserPermission> updatedPermissions
    ) {

        Set<UserPermission> notebookAccessList = notebook.getAccessList();
        HashSet<UserPermission> updatedNotebookPermissions = new HashSet<>();

        for (UserPermission updatedPermission : updatedPermissions) {
            UserPermission currentUserPermission
                    = findPermissionsByUserId(notebookAccessList, updatedPermission.getUser().getId());
            if (currentUserPermission == null) {

                notebookAccessList.add(updatedPermission);
                updatedNotebookPermissions.add(updatedPermission);

            } else if (updatedPermission.getPermissionCreationLevel().equals(NOTEBOOK)
                    || !currentUserPermission.getPermissionCreationLevel().equals(NOTEBOOK)) {

                currentUserPermission.setPermissions(updatedPermission.getPermissions());
                updatedNotebookPermissions.add(currentUserPermission);
            }
        }

        if (!updatedNotebookPermissions.isEmpty()) {
            List<Experiment> changedExperiments = notebook.getExperiments().stream()
                    .filter(experiment ->
                            ExperimentPermissionHelper
                                    .updatePermissionFromNotebook(experiment, updatedNotebookPermissions))
                    .collect(toList());

            return Pair.of(notebook, changedExperiments);
        }
        return null;
    }

    static void removePermissionFromProject(Notebook notebook, Set<UserPermission> removedPermissions) {
        notebook.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        notebook.getExperiments().forEach(experiment ->
                ExperimentPermissionHelper.removePermissionsFromNotebook(experiment, removedPermissions));
    }

    /**
     * Cascade updates permission from experiment.
     *
     * @param project                      project witch contains experiment
     * @param notebook                     notebook witch contains experiment
     * @param addedToExperimentPermissions permissions witch added to experiment
     *                                     and now should be added to notebook and project
     * @return pair of wasProjectChanged and wasNotebookChanged
     */
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
     * @return {@code Pair.of(wasProjectChanged, changedExperiments)}
     */
    public static Pair<Boolean, List<Experiment>> changeNotebookPermissions(Project project,
                                                                            Notebook notebook,
                                                                            Set<UserPermission> newUserPermissions
    ) {
        boolean projectHadChanged = false;
        boolean allExperimentsWereChanged = false;
        List<Experiment> changedExperiments = Collections.emptyList();

        Set<UserPermission> createdPermissions = getCreatedPermission(notebook, newUserPermissions, NOTEBOOK);
        if (!createdPermissions.isEmpty()) {

            allExperimentsWereChanged = true;
            projectHadChanged = addPermissions(project, notebook, createdPermissions);
        }

        Set<UserPermission> updatedPermissions = getUpdatedPermissions(notebook, newUserPermissions, NOTEBOOK);

        if (!updatedPermissions.isEmpty()) {

            Pair<Notebook, List<Experiment>> updatePermissions = updatePermissions(notebook, updatedPermissions);
            changedExperiments = updatePermissions != null ? updatePermissions.getValue() : Collections.emptyList();
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(notebook, newUserPermissions);

        if (!removedPermissions.isEmpty()) {

            allExperimentsWereChanged = true;
            projectHadChanged |= removePermissions(project, notebook, removedPermissions);
        }
        return Pair.of(projectHadChanged, allExperimentsWereChanged ? notebook.getExperiments() : changedExperiments);
    }

    public static void fillNewNotebooksPermissions(Project project, Notebook notebook, User creator) {
        Set<UserPermission> accessList = notebook.getAccessList();
        notebook.setAccessList(new HashSet<>());
        PermissionUtil.addOwnerToAccessList(notebook.getAccessList(), creator, NOTEBOOK);
        PermissionUtil.addUsersFromUpperLevel(
                notebook.getAccessList(), project.getAccessList(), PermissionCreationLevel.PROJECT);

        notebook.setExperiments(Collections.emptyList());
        updatePermissions(notebook, getUpdatedPermissions(notebook, accessList, NOTEBOOK));
        addPermissions(project, notebook, getCreatedPermission(notebook, accessList, NOTEBOOK));
    }
}
