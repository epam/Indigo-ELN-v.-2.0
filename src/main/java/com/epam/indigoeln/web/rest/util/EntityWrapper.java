package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.web.rest.util.PermissionUtil.equalsByUserId;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * The wrapper class for Tree structure of BasicModelObjects.
 */
@Getter
abstract class EntityWrapper {

    protected EntityWrapper parent;

    protected List<EntityWrapper> children;

    protected BasicModelObject value;

    protected PermissionCreationLevel permissionCreationLevel;

    private static void changePermission(UserPermission permission,
                                         UserPermission userPermissionOuterEntity,
                                         Set<UserPermission> updated,
                                         PermissionCreationLevel permissionCreationLevel  //updating aggregation
    ) {
        if (PermissionUtil.canBeChangedFromThisLevel(
                userPermissionOuterEntity.getPermissionCreationLevel(), permissionCreationLevel)) {
            userPermissionOuterEntity.setPermissions(
                    UserPermission.OWNER.equals(permission.getPermissionView())
                            ? permission.getPermissions()
                            : UserPermission.VIEWER_PERMISSIONS);

            updated.add(userPermissionOuterEntity);
        }
    }

    void removePermissionsDown(Set<UserPermission> removedPermissions) {

        children.forEach(child -> child.removePermissionsDown(removedPermissions));

        value.getAccessList().removeIf(oldPermission -> removedPermissions.stream()
                .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));
    }

    void addPermissionsDown(Set<UserPermission> createdPermissions) {

        children.forEach(child -> child.addPermissionsDown(createdPermissions));

        value.getAccessList().addAll(createdPermissions);
    }

    void updatePermissionsDown(Set<UserPermission> updatedPermissions) {

        children.forEach(child -> child.updatePermissionsDown(updatedPermissions));

        value.getAccessList().removeIf(oldPermission ->
                updatedPermissions.stream()
                        .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));

        value.getAccessList().addAll(updatedPermissions);
    }

    Set<UserPermission> addPermissionsUp(Set<UserPermission> createdPermissions) {
        Set<UserPermission> added = new HashSet<>();

        for (UserPermission userPermission : createdPermissions) {
            UserPermission userPermissionOuterEntity = PermissionUtil.findPermissionsByUserId(
                    parent.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                parent.getValue().getAccessList().add(
                        UserPermission.OWNER.equals(userPermission.getPermissionView())
                                ? userPermission
                                : userPermission.setPermissions(UserPermission.VIEWER_PERMISSIONS));
                added.add(userPermission);
            } else {
                changePermission(userPermission, userPermissionOuterEntity, added, this.getPermissionCreationLevel());
            }
        }

        return added;
    }

    Set<UserPermission> updatePermissionsUp(Set<UserPermission> updatedPermissions) {
        Set<UserPermission> updated = new HashSet<>();

        for (UserPermission permission : updatedPermissions) {
            UserPermission userPermissionOuterEntity = PermissionUtil.findPermissionsByUserId(
                    parent.getValue().getAccessList(), permission.getUser().getId());
            if (userPermissionOuterEntity != null) {
                changePermission(permission, userPermissionOuterEntity, updated, this.getPermissionCreationLevel());
            }
        }
        return updated;
    }

    Set<UserPermission> removePermissionsUp(Set<UserPermission> removedPermissions) {
        Set<UserPermission> canBeRemovedFromInnerEntity = parent.getValue().getAccessList().stream()
                .filter(userPermission -> PermissionUtil.canBeChangedFromThisLevel(
                        userPermission.getPermissionCreationLevel(), this.getPermissionCreationLevel()))
                .collect(toSet());

        Set<UserPermission> containsInProjectsNotebooks = parent.getChildren().stream()
                .filter(child -> !child.getValue().getId().equals(value.getId()))
                .flatMap(child -> child.getValue().getAccessList().stream())
                .collect(toSet());

        removedPermissions.removeIf(removingPermission -> userPresentInInnerEntitiesOrCantBeRemovedFromInnerEntity(
                canBeRemovedFromInnerEntity, containsInProjectsNotebooks, removingPermission));

        parent.removePermissionsDown(removedPermissions);

        return removedPermissions;
    }

    private static boolean userPresentInInnerEntitiesOrCantBeRemovedFromInnerEntity(
            Set<UserPermission> canBeRemovedFromInnerEntity,
            Set<UserPermission> containsInProjectsNotebooks,
            UserPermission removingPermission
    ) {
        return containsInProjectsNotebooks.stream()
                .anyMatch(cantBeRemoved -> equalsByUserId(cantBeRemoved, removingPermission))
                || canBeRemovedFromInnerEntity.stream()
                .noneMatch(canBeRemoved -> equalsByUserId(canBeRemoved, removingPermission));
    }

    @NoArgsConstructor
    public static class ProjectWrapper extends EntityWrapper {

        ProjectWrapper(Project project) {
            this.parent = null;
            this.children = project.getNotebooks().stream()
                    .map(notebook -> new NotebookWrapper(this, notebook))
                    .collect(toList());
            this.value = project;
            this.permissionCreationLevel = PermissionCreationLevel.PROJECT;
        }

        private static ProjectWrapper parentForNotebookWrapper(Project project, NotebookWrapper notebookWrapper) {
            ProjectWrapper wrapper = new ProjectWrapper();
            wrapper.parent = null;
            wrapper.children = Collections.singletonList(notebookWrapper);
            wrapper.value = project;
            wrapper.permissionCreationLevel = PermissionCreationLevel.PROJECT;
            return wrapper;
        }
    }

    @NoArgsConstructor
    public static class NotebookWrapper extends EntityWrapper {

        NotebookWrapper(Project project, Notebook notebook) {
            this.parent = ProjectWrapper.parentForNotebookWrapper(project, this);
            this.children = notebook.getExperiments().stream()
                    .map(experiment -> new ExperimentWrapper(this, experiment))
                    .collect(toList());
            this.value = notebook;
            this.permissionCreationLevel = PermissionCreationLevel.NOTEBOOK;
        }

        private NotebookWrapper(ProjectWrapper parent, Notebook notebook) {
            this.parent = parent;
            this.children = notebook.getExperiments().stream()
                    .map(experiment -> new ExperimentWrapper(this, experiment))
                    .collect(toList());
            this.value = notebook;
            this.permissionCreationLevel = PermissionCreationLevel.NOTEBOOK;
        }

        private static NotebookWrapper parentForExperimentWrapper(Project project, Notebook notebook, ExperimentWrapper experimentWrapper) {
            NotebookWrapper wrapper = new NotebookWrapper();
            wrapper.parent = ProjectWrapper.parentForNotebookWrapper(project, wrapper);
            wrapper.children = Collections.singletonList(experimentWrapper);
            wrapper.value = notebook;
            wrapper.permissionCreationLevel = PermissionCreationLevel.NOTEBOOK;
            return wrapper;
        }
    }

    public static class ExperimentWrapper extends EntityWrapper {

        ExperimentWrapper(Project project, Notebook notebook, Experiment experiment) {
            this.parent = NotebookWrapper.parentForExperimentWrapper(project, notebook, this);
            this.value = experiment;
            this.children = Collections.emptyList();
            this.permissionCreationLevel = PermissionCreationLevel.EXPERIMENT;
        }

        private ExperimentWrapper(NotebookWrapper parent, Experiment experiment) {
            this.parent = parent;
            this.value = experiment;
            this.children = Collections.emptyList();
            this.permissionCreationLevel = PermissionCreationLevel.EXPERIMENT;
        }
    }
}
