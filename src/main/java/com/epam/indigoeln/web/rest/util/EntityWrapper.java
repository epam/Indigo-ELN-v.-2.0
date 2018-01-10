package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.security.Authority;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.epam.indigoeln.web.rest.util.PermissionUtil.equalsByUserId;
import static com.epam.indigoeln.web.rest.util.PermissionUtil.isContentEditor;
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

    protected Function<Set<UserPermission>, Set<UserPermission>> permissionsPreProcessing;

    private static void changePermission(UserPermission permission,
                                         UserPermission userPermissionOuterEntity,
                                         Set<UserPermission> updated, //updating aggregation
                                         PermissionCreationLevel creationLevel
    ) {
        if (PermissionUtil.canBeChangedFromThisLevel(
                userPermissionOuterEntity.getPermissionCreationLevel(), creationLevel)) {
            userPermissionOuterEntity.setPermissions(
                    UserPermission.OWNER.equals(permission.getPermissionView())
                            || UserPermission.USER.equals(permission.getPermissionView())
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

        Set<UserPermission> permissions = permissionsPreProcessing.apply(createdPermissions);

        value.getAccessList().addAll(permissions);

        children.forEach(child -> child.addPermissionsDown(permissions));
    }

    void updatePermissionsDown(Set<UserPermission> updatedPermissions) {

        Set<UserPermission> permissions = permissionsPreProcessing.apply(updatedPermissions);

        children.forEach(child -> child.updatePermissionsDown(permissions));

        value.getAccessList().removeIf(oldPermission ->
                permissions.stream()
                        .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));

        value.getAccessList().addAll(permissions);
    }

    Set<UserPermission> addOrUpdatePermissionsUp(Set<UserPermission> createdPermissions) {
        Set<UserPermission> added = new HashSet<>();

        for (UserPermission userPermission : createdPermissions) {
            UserPermission userPermissionOuterEntity = PermissionUtil.findPermissionsByUserId(
                    parent.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                parent.getValue().getAccessList().add(
                        UserPermission.OWNER.equals(userPermission.getPermissionView())
                                || UserPermission.USER.equals(userPermission.getPermissionView())
                                ? userPermission
                                : userPermission.setPermissions(UserPermission.VIEWER_PERMISSIONS));
                added.add(userPermission);
            } else {
                changePermission(userPermission, userPermissionOuterEntity, added, permissionCreationLevel);
            }
        }

        return added;
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

    public static class NotebookWrapper extends EntityWrapper {

        private NotebookWrapper() {
            permissionsPreProcessing = NotebookWrapper::notebookPermissionPreProcessing;
            permissionCreationLevel = PermissionCreationLevel.NOTEBOOK;
        }

        NotebookWrapper(Project project, Notebook notebook) {
            this();
            this.parent = ProjectWrapper.parentForNotebookWrapper(project, this);
            this.children = notebook.getExperiments().stream()
                    .map(experiment -> new ExperimentWrapper(this, experiment))
                    .collect(toList());
            this.value = notebook;
        }

        private NotebookWrapper(ProjectWrapper parent, Notebook notebook) {
            this();
            this.parent = parent;
            this.children = notebook.getExperiments().stream()
                    .map(experiment -> new ExperimentWrapper(this, experiment))
                    .collect(toList());
            this.value = notebook;
        }

        private static NotebookWrapper parentForExperimentWrapper(Project project, Notebook notebook, ExperimentWrapper experimentWrapper) {
            NotebookWrapper wrapper = new NotebookWrapper();
            wrapper.parent = ProjectWrapper.parentForNotebookWrapper(project, wrapper);
            wrapper.children = Collections.singletonList(experimentWrapper);
            wrapper.value = notebook;
            return wrapper;
        }

        private static Set<UserPermission> notebookPermissionPreProcessing(Set<UserPermission> createdPermissions) {
            return createdPermissions.stream().map(userPermission ->
                    //we don't change permissions for ContentEditors
                    !isContentEditor(userPermission.getUser())
                            //user without NOTEBOOK_CREATOR Authority can only have VIEWER_PERMISSIONS for Notebook
                            && ((!userPermission.getPermissionView().equals(UserPermission.VIEWER) &&
                            !userPermission.getUser().getAuthorities().contains(Authority.NOTEBOOK_CREATOR))
                            //if user is not EXPERIMENT_CREATOR he can't have USER_PERMISSIONS for Notebook
                            || (userPermission.getPermissionView().equals(UserPermission.USER) &&
                            !userPermission.getUser().getAuthorities().contains(Authority.EXPERIMENT_CREATOR))) ?
                            new UserPermission(userPermission.getUser(), UserPermission.VIEWER_PERMISSIONS) :
                            userPermission)
                    .collect(toSet());
        }
    }

    public static class ExperimentWrapper extends EntityWrapper {

        private ExperimentWrapper() {
            permissionsPreProcessing = ExperimentWrapper::experimentsPermissionPreProcessing;
            permissionCreationLevel = PermissionCreationLevel.EXPERIMENT;
        }

        ExperimentWrapper(Project project, Notebook notebook, Experiment experiment) {
            this();
            this.parent = NotebookWrapper.parentForExperimentWrapper(project, notebook, this);
            this.value = experiment;
            this.children = Collections.emptyList();
        }

        private ExperimentWrapper(NotebookWrapper parent, Experiment experiment) {
            this();
            this.parent = parent;
            this.value = experiment;
            this.children = Collections.emptyList();
        }

        private static Set<UserPermission> experimentsPermissionPreProcessing(Set<UserPermission> createdPermissions) {
            return createdPermissions.stream()
                    .filter(userPermission ->
                            userPermission.getUser().getAuthorities().contains(Authority.EXPERIMENT_READER))
                    .map(userPermission ->
                            //we don't change permissions for ContentEditors
                            !isContentEditor(userPermission.getUser())
                                    //there is no USER_PERMISSIONS for Experiment
                                    && (userPermission.getPermissions().equals(UserPermission.USER_PERMISSIONS)
                                    //user without EXPERIMENT_CREATOR Authority can't have OWNER_PERMISSIONS
                                    || (userPermission.getPermissions().equals(UserPermission.OWNER_PERMISSIONS)
                                    && !userPermission.getUser().getAuthorities().contains(Authority.EXPERIMENT_CREATOR))) ?
                                    new UserPermission(userPermission.getUser(), UserPermission.VIEWER_PERMISSIONS) :
                                    userPermission)
                    .collect(toSet());
        }
    }
}
