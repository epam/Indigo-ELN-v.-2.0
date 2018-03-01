/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.NOTEBOOK;
import static com.epam.indigoeln.core.model.PermissionCreationLevel.PROJECT;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
import static com.epam.indigoeln.web.rest.util.permission.helpers.PermissionChanges.ofCreatedPermissions;
import static java.util.Collections.emptyList;
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
    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    addPermissions(Project project, Notebook notebook, Set<UserPermission> addedPermissions) {

        Set<UserPermission> permissionsAddedToNotebook = PermissionUtil.addAllIfNotPresent(notebook, addedPermissions);

        List<PermissionChanges<Experiment>> experimentsChanges = notebook.getExperiments().stream().map(experiment ->
                ExperimentPermissionHelper
                        .addPermissionsFromNotebook(experiment, permissionsAddedToNotebook)).collect(toList());

        PermissionChanges<Project> projectChanges = ProjectPermissionHelper
                .addPermissionsFromNotebook(project, addedPermissions);

        return Triple.of(projectChanges,
                PermissionChanges.ofCreatedPermissions(notebook, permissionsAddedToNotebook),
                experimentsChanges);
    }

    public static Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    updatePermissions(Notebook notebook,
                      Set<UserPermission> updatedPermissions
    ) {

        return updatePermissionFromProject(notebook, updatedPermissions);
    }

    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    removePermissions(Project project, Notebook notebook, Set<UserPermission> removedPermission) {

        Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> notebookAndExperimentsChanges =
                removePermissionFromProject(notebook, removedPermission);

        Set<UserPermission> canBeRemovedFromProject = removedPermission.stream()
                .filter(permission ->
                        !permission.getPermissionCreationLevel().equals(PROJECT))
                .collect(toSet());

        PermissionChanges<Project> projectChanges =
                ProjectPermissionHelper.removePermissionFromNotebook(project, notebook, canBeRemovedFromProject);

        return Triple.of(
                projectChanges,
                notebookAndExperimentsChanges.getLeft(),
                notebookAndExperimentsChanges.getRight());
    }


    static Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    addPermissionsFromProject(Notebook notebook, Set<UserPermission> userPermissions) {

        Set<UserPermission> addedToNotebookPermissions = PermissionUtil.addAllIfNotPresent(notebook, userPermissions);

        List<PermissionChanges<Experiment>> experimentsChanges = emptyList();
        if (!addedToNotebookPermissions.isEmpty()) {
            experimentsChanges = notebook.getExperiments().stream()
                    .map(experiment ->
                            ExperimentPermissionHelper
                                    .addPermissionsFromNotebook(experiment, addedToNotebookPermissions))
                    .collect(toList());
        }
        return Pair.of(
                PermissionChanges.ofCreatedPermissions(notebook, addedToNotebookPermissions),
                experimentsChanges);
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
    @NonNull
    static Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> updatePermissionFromProject(
            Notebook notebook,
            Set<UserPermission> updatedPermissions
    ) {

        Set<UserPermission> notebookAccessList = notebook.getAccessList();
        Set<UserPermission> updatedNotebookPermissions = new HashSet<>();

        for (UserPermission updatedPermission : updatedPermissions) {
            UserPermission currentUserPermission
                    = findPermissionsByUserId(notebookAccessList, updatedPermission.getUser().getId());
            if (currentUserPermission == null) {

                notebookAccessList.add(updatedPermission);
                updatedNotebookPermissions.add(updatedPermission);

            } else if (updatedPermission.getPermissionCreationLevel().equals(NOTEBOOK)
                    || !currentUserPermission.getPermissionCreationLevel().equals(NOTEBOOK)) {

                currentUserPermission.setPermissions(updatedPermission.getPermissions());
                currentUserPermission.setPermissionCreationLevel(updatedPermission.getPermissionCreationLevel());
                updatedNotebookPermissions.add(currentUserPermission);
            }
        }

        List<PermissionChanges<Experiment>> changedExperiments = emptyList();
        if (!updatedNotebookPermissions.isEmpty()) {
            changedExperiments = notebook.getExperiments().stream()
                    .map(experiment ->
                            ExperimentPermissionHelper
                                    .updatePermissionFromNotebook(experiment, updatedNotebookPermissions))
                    .collect(toList());
        }
        return Pair.of(
                PermissionChanges.ofUpdatedPermissions(notebook, updatedNotebookPermissions),
                changedExperiments);
    }

    static Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> removePermissionFromProject(
            Notebook notebook,
            Set<UserPermission> removedPermissions
    ) {

        Set<UserPermission> permissionsRemovedFromNotebook =
                removeIf(notebook.getAccessList(), userPermission -> hasUser(removedPermissions, userPermission));

        List<PermissionChanges<Experiment>> experimentsChanges = notebook.getExperiments().stream()
                .map(experiment ->
                        ExperimentPermissionHelper.removePermissionsFromNotebook(experiment, removedPermissions))
                .collect(toList());

        return Pair.of(
                PermissionChanges.ofRemovedPermissions(notebook, permissionsRemovedFromNotebook),
                experimentsChanges);
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
    public static Pair<PermissionChanges<Project>, PermissionChanges<Notebook>>
    addPermissionsFromExperiment(Project project,
                                 Notebook notebook,
                                 Set<UserPermission> addedToExperimentPermissions
    ) {
        Set<UserPermission> addedToNotebook = addAllAsViewerIfNotPresent(notebook, addedToExperimentPermissions);

        PermissionChanges<Project> projectWasUpdated = ProjectPermissionHelper
                .addPermissionsFromNotebook(project, addedToNotebook);

        return Pair.of(
                projectWasUpdated,
                ofCreatedPermissions(notebook, addedToNotebook));
    }

    public static Pair<PermissionChanges<Project>, PermissionChanges<Notebook>>
    removePermissionFromExperiment(Project project,
                                   Notebook notebook,
                                   Experiment fromExperiment,
                                   Set<UserPermission> removedPermissions
    ) {

        Set<UserPermission> presentedInOtherExperiments = notebook.getExperiments().stream()
                .filter(experiment -> !experiment.getId().equals(fromExperiment.getId()))
                .flatMap(experiment -> experiment.getAccessList().stream())
                .collect(toSet());

        Set<UserPermission> canBeRemovedFromExperimentLevel =
                removedPermissions.stream()
                        .filter(removedPermission ->
                                !hasUser(presentedInOtherExperiments, removedPermission))
                        .collect(toSet());

        Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> changes =
                removePermissions(project, notebook, canBeRemovedFromExperimentLevel);

        return Pair.of(changes.getLeft(), changes.getMiddle());
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
     * @param authorOfChanges    author of permissions changes
     * @return {@code tuple of projectChanges, notebookChanges and experimentsChanges)}
     */
    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    changeNotebookPermissions(Project project,
                              Notebook notebook,
                              Set<UserPermission> newUserPermissions,
                              User authorOfChanges
    ) {
        List<PermissionChanges<Experiment>> changedExperiments = new ArrayList<>(notebook.getExperiments().size());
        PermissionChanges<Notebook> notebookPermissionChanges = new PermissionChanges<>(notebook);
        PermissionChanges<Project> projectPermissionChanges = new PermissionChanges<>(project);

        Set<UserPermission> createdPermissions = getCreatedPermission(notebook, newUserPermissions, NOTEBOOK);
        if (!createdPermissions.isEmpty()) {
            Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
                    changes = addPermissions(project, notebook, createdPermissions);

            changedExperiments.addAll(changes.getRight());
            notebookPermissionChanges.merge(changes.getMiddle());
            projectPermissionChanges.merge(changes.getLeft());

        }

        Set<UserPermission> updatedPermissions =
                getUpdatedPermissions(notebook, newUserPermissions, NOTEBOOK, authorOfChanges);

        if (!updatedPermissions.isEmpty()) {

            Pair<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> changes =
                    updatePermissions(notebook, updatedPermissions);

            changedExperiments.addAll(changes.getRight());
            notebookPermissionChanges.merge(changes.getLeft());
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(notebook, newUserPermissions);

        if (!removedPermissions.isEmpty()) {
            Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> changes =
                    removePermissions(project, notebook, removedPermissions);

            changedExperiments.addAll(changes.getRight());
            notebookPermissionChanges.merge(changes.getMiddle());
            projectPermissionChanges.merge(changes.getLeft());
        }
        return Triple.of(projectPermissionChanges, notebookPermissionChanges, changedExperiments);
    }

    public static Triple<PermissionChanges<Project>, PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>
    fillNewNotebooksPermissions(Project project, Notebook notebook, User creator) {
        Set<UserPermission> permissions = notebook.getAccessList();
        notebook.setAccessList(new HashSet<>());
        PermissionUtil.addOwnerToAccessList(permissions, creator, NOTEBOOK);
        PermissionUtil.addUsersFromUpperLevel(permissions, project.getAccessList(), PROJECT);

        notebook.setExperiments(emptyList());
        return changeNotebookPermissions(project, notebook, permissions, creator);
    }
}
