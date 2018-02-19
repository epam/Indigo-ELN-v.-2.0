package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.PROJECT;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ProjectPermissionHelper {

    private ProjectPermissionHelper() {
    }

    /**
     * Add all permissions from createdPermissions to the project and its notebooks and experiments.
     *
     * @param project            updating project
     * @param createdPermissions new {@link UserPermission}s for project
     */
    public static Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
    addPermissions(Project project, Set<UserPermission> createdPermissions) {

        Set<UserPermission> addedToProjectPermissions = PermissionUtil.addAllIfNotPresent(project, createdPermissions);

        Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> notebookAndExperimentsChanges = emptyMap();
        if (!addedToProjectPermissions.isEmpty()) {
            notebookAndExperimentsChanges =
                    project.getNotebooks().stream()
                            .map(notebook ->
                                    NotebookPermissionHelper
                                            .addPermissionsFromProject(notebook, addedToProjectPermissions))
                            .collect(toMap(Pair::getKey, Pair::getValue));
        }
        return Pair.of(
                PermissionChanges.ofCreatedPermissions(project, addedToProjectPermissions),
                notebookAndExperimentsChanges
        );
    }

    public static Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
    updatePermissions(Project project, Set<UserPermission> updatedPermissions) {

        project.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        Set<UserPermission> updatesProjectPermissions = PermissionUtil.addAllIfNotPresent(project, updatedPermissions);

        Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> notebookAndExperimentsChanges =
                project.getNotebooks().stream()
                        .map(notebook ->
                                NotebookPermissionHelper
                                        .updatePermissionFromProject(notebook, updatesProjectPermissions))
                        .collect(toMap(Pair::getLeft, Pair::getRight));

        return Pair.of(
                PermissionChanges.ofUpdatedPermissions(project, updatesProjectPermissions),
                notebookAndExperimentsChanges
        );
    }

    public static Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
    removePermission(Project project, Set<UserPermission> removedPermissions) {

        Set<UserPermission> removedFromProjectPermissions =
                removeIf(project.getAccessList(), userPermission -> hasUser(removedPermissions, userPermission));

        Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> notebookAndExperimentChanges =
                project.getNotebooks().stream().map(notebook ->
                        NotebookPermissionHelper.removePermissionFromProject(notebook, removedFromProjectPermissions))
                        .collect(toMap(Pair::getLeft, Pair::getRight));

        return Pair.of(
                PermissionChanges.ofRemovedPermissions(project, removedFromProjectPermissions),
                notebookAndExperimentChanges);
    }

    static PermissionChanges<Project> addPermissionsFromNotebook(Project project, Set<UserPermission> addedToNotebookPermissions) {

        Set<UserPermission> addedToProjectPermissions = addAllAsViewerIfNotPresent(project, addedToNotebookPermissions);
        return PermissionChanges.ofCreatedPermissions(project, addedToProjectPermissions);
    }

    static PermissionChanges<Project> removePermissionFromNotebook(Project project,
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

        Set<UserPermission> removedFromProjectPermissions =
                removeIf(project.getAccessList(), userPermission -> hasUser(canBeRemovedFromProject, userPermission));

        return PermissionChanges.ofRemovedPermissions(project, removedFromProjectPermissions);

    }

    /**
     * Change projects access list according to {@code newUserPermissions}.
     *
     * @param project            project to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return Map of updated notebooks and their updated experiments
     */
    public static Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>> changeProjectPermissions(
            Project project,
            Set<UserPermission> newUserPermissions
    ) {

        Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> changedNotebooksAndExperiments = new HashMap<>();
        PermissionChanges<Project> projectPermissionChanges = new PermissionChanges<>(project);

        Set<UserPermission> createdPermissions = getCreatedPermission(project, newUserPermissions, PROJECT);

        if (!createdPermissions.isEmpty()) {

            Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
                    changes = addPermissions(project, createdPermissions);

            changedNotebooksAndExperiments.putAll(changes.getValue());
            projectPermissionChanges.merge(changes.getLeft());
        }

        Set<UserPermission> updatedPermissions = getUpdatedPermissions(project, newUserPermissions, PROJECT);

        if (!updatedPermissions.isEmpty()) {

            Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
                    changes = updatePermissions(project, updatedPermissions);

            addChangesToMap(changedNotebooksAndExperiments, changes.getRight());
            projectPermissionChanges.merge(changes.getLeft());
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(project, newUserPermissions);

        if (!removedPermissions.isEmpty()) {

            Pair<PermissionChanges<Project>, Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>>>
                    changes = removePermission(project, removedPermissions);

            addChangesToMap(changedNotebooksAndExperiments, changes.getRight());
            projectPermissionChanges.merge(changes.getLeft());
        }

        return Pair.of(projectPermissionChanges, changedNotebooksAndExperiments);
    }

    private static void addChangesToMap(
            Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> origin,
            Map<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> notebookAndExperimentChanges
    ) {
        for (Map.Entry<PermissionChanges<Notebook>, List<PermissionChanges<Experiment>>> entry
                : notebookAndExperimentChanges.entrySet()) {

            origin.merge(entry.getKey(), entry.getValue(), (listOne, listTwo) -> {
                List<PermissionChanges<Experiment>> newList = new ArrayList<>(listOne);
                newList.addAll(listTwo);
                return newList;
            });
        }
    }

    public static PermissionChanges<Project> fillNewProjectPermissions(Project project, User user) {
        Set<UserPermission> accessList = project.getAccessList();
        project.setAccessList(new HashSet<>());
        project.setNotebooks(Collections.emptyList());
        PermissionUtil.addOwnerToAccessList(project.getAccessList(), user, PROJECT);

        return addPermissions(project, getCreatedPermission(project, accessList, PROJECT)).getLeft();
    }
}
