package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.UserPermission;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.PROJECT;
import static com.epam.indigoeln.core.model.UserPermission.VIEWER_PERMISSIONS;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.*;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ProjectPermissionHelper {

    private ProjectPermissionHelper() {
    }

    /**
     * Add all permissions from createdPermissions to the project and its notebooks and experiments.
     *
     * @param project
     * @param createdPermissions
     */
    public static boolean addPermissions(Project project, Set<UserPermission> createdPermissions) {
        if (project.getAccessList().addAll(createdPermissions)) {
            project.getNotebooks().forEach(notebook ->
                    NotebookPermissionHelper.addPermissionsFromProject(notebook, createdPermissions));
            return true;
        }
        return false;
    }

    public static Map<Notebook, List<Experiment>> updatePermissions(Project project, Set<UserPermission> updatedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(updatedPermissions, userPermission));
        project.getAccessList().addAll(updatedPermissions);

        return project.getNotebooks().stream()
                .map(notebook ->
                        NotebookPermissionHelper.updatePermissionFromProject(notebook, updatedPermissions))
                .filter(Objects::nonNull)
                .collect(toMap(Pair::getLeft, Pair::getRight));
    }

    public static void removePermission(Project project, Set<UserPermission> removedPermissions) {
        project.getAccessList().removeIf(userPermission -> hasUser(removedPermissions, userPermission));

        project.getNotebooks().forEach(notebook ->
                NotebookPermissionHelper.removePermissionFromProject(notebook, removedPermissions));
    }

    static boolean addPermissionsFromNotebook(Project project, Set<UserPermission> addedPermissions) {

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

    static boolean removePermissionFromNotebook(Project project,
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

        return project.getAccessList().removeIf(userPermission -> hasUser(canBeRemovedFromProject, userPermission));
    }

    /**
     * Change projects access list according to {@code newUserPermissions}.
     *
     * @param project            project to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return Map of updated notebooks and their updated experiments
     */
    public static Map<Notebook, List<Experiment>> changeProjectPermissions(Project project,
                                                                           Set<UserPermission> newUserPermissions
    ) {

        boolean allExperimentsAndNotebooksWasChanged = false;
        Map<Notebook, List<Experiment>> changedNotebooksAndExperiments = new HashMap<>();

        Set<UserPermission> createdPermissions = getCreatedPermission(project, newUserPermissions, PROJECT);

        if (!createdPermissions.isEmpty()) {

            allExperimentsAndNotebooksWasChanged = true;
            addPermissions(project, createdPermissions);
        }

        Set<UserPermission> updatedPermissions = getUpdatedPermissions(project, newUserPermissions, PROJECT);

        if (!updatedPermissions.isEmpty()) {

            changedNotebooksAndExperiments = updatePermissions(project, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = getRemovedPermissions(project, newUserPermissions);

        if (!removedPermissions.isEmpty()) {

            allExperimentsAndNotebooksWasChanged = true;
            removePermission(project, removedPermissions);
        }

        return allExperimentsAndNotebooksWasChanged ?
                allExperimentsAndNotebooks(project) :
                changedNotebooksAndExperiments;
    }

    private static Map<Notebook, List<Experiment>> allExperimentsAndNotebooks(Project project) {
        return project.getNotebooks().stream().collect(toMap(Function.identity(), Notebook::getExperiments));
    }
}
