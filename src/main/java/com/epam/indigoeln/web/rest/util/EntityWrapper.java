package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.security.Authority;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.epam.indigoeln.core.model.UserPermission.*;
import static com.epam.indigoeln.core.security.Authority.*;
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
    protected Function<Set<UserPermission>, Set<UserPermission>> permissionsUpPreProcessing;

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
        Set<UserPermission> permissions = permissionsUpPreProcessing.apply(createdPermissions);

        for (UserPermission userPermission : permissions) {
            UserPermission userPermissionOuterEntity = PermissionUtil.findPermissionsByUserId(
                    parent.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                parent.getValue().getAccessList().add(userPermission);
            } else {
                changePermission(userPermission, userPermissionOuterEntity, permissionCreationLevel);
            }
        }
        return permissions;
    }

    Set<UserPermission> removePermissionsUp(Set<UserPermission> removedPermissions) {
        Set<UserPermission> canBeRemovedFromInnerEntity = parent.getValue().getAccessList().stream()
                .filter(userPermission -> PermissionUtil.canBeChangedFromThisLevel(
                        userPermission.getPermissionCreationLevel(), this.getPermissionCreationLevel()))
                .collect(toSet());

        Set<UserPermission> containsInParentsChildren = parent.getChildren().stream()
                .filter(child -> !child.getValue().getId().equals(value.getId()))
                .flatMap(child -> child.getValue().getAccessList().stream())
                .collect(toSet());

        removedPermissions.removeIf(removingPermission -> userPresentInInnerEntitiesOrCantBeRemovedFromInnerEntity(
                canBeRemovedFromInnerEntity, containsInParentsChildren, removingPermission));

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

    private static void changePermission(UserPermission permission,
                                         UserPermission userPermissionOuterEntity,
                                         PermissionCreationLevel creationLevel
    ) {
        if (PermissionUtil.canBeChangedFromThisLevel(
                userPermissionOuterEntity.getPermissionCreationLevel(), creationLevel)) {
            userPermissionOuterEntity.setPermissions(permission.getPermissions());
        }
    }

    private static UserPermission shouldHavePermission(UserPermission userPermission,
                                                       List<Authority> ownersAuthorities,
                                                       List<Authority> usersAuthorities
    ) {
        //we don't change permissions for ContentEditors
        if (isContentEditor(userPermission.getUser())) {
            return userPermission;
        }
        String currentPermissionsView = userPermission.getPermissionView();
        if (OWNER.equals(currentPermissionsView)) {
            if (userPermission.getUser().getAuthorities().containsAll(ownersAuthorities))
                return userPermission;

            if (userPermission.getUser().getAuthorities().containsAll(usersAuthorities))
                return getUsersUserPermission(userPermission);

            return getViewerUserPermission(userPermission);
        } else if (USER.equals(currentPermissionsView)) {
            if (userPermission.getUser().getAuthorities().containsAll(usersAuthorities))
                return getUsersUserPermission(userPermission);

            return getViewerUserPermission(userPermission);
        } else {
            return getViewerUserPermission(userPermission);
        }
    }

    private static UserPermission getUsersUserPermission(UserPermission userPermission) {
        return userPermission.getPermissionView().equals(USER) ?
                userPermission :
                new UserPermission(userPermission.getUser(), USER_PERMISSIONS,
                        userPermission.getPermissionCreationLevel());
    }

    private static UserPermission getViewerUserPermission(UserPermission userPermission) {
        return userPermission.getPermissionView().equals(VIEWER) ?
                userPermission :
                new UserPermission(userPermission.getUser(), VIEWER_PERMISSIONS,
                        userPermission.getPermissionCreationLevel());
    }

    public static class ProjectWrapper extends EntityWrapper {

        private static final List<Authority> OWNERS_AUTHORITIES;

        private static final List<Authority> USERS_AUTHORITIES;

        static {
            OWNERS_AUTHORITIES = Arrays.asList(PROJECT_READER, PROJECT_CREATOR, NOTEBOOK_READER, NOTEBOOK_CREATOR);
            USERS_AUTHORITIES = Arrays.asList(PROJECT_READER, NOTEBOOK_READER, NOTEBOOK_CREATOR);
        }

        private ProjectWrapper() {
            parent = null;
            permissionCreationLevel = PermissionCreationLevel.PROJECT;
            permissionsPreProcessing = ProjectWrapper::projectPermissionPreProcessing;
            permissionsUpPreProcessing = Function.identity();
        }

        ProjectWrapper(Project project) {
            this();
            this.children = project.getNotebooks().stream()
                    .map(notebook -> new NotebookWrapper(this, notebook))
                    .collect(toList());
            this.value = project;
        }

        private static ProjectWrapper parentForNotebookWrapper(Project project, NotebookWrapper notebookWrapper) {
            ProjectWrapper wrapper = new ProjectWrapper();
            wrapper.children = Collections.singletonList(notebookWrapper);
            wrapper.value = project;
            wrapper.permissionCreationLevel = PermissionCreationLevel.PROJECT;
            return wrapper;
        }

        private static Set<UserPermission> projectPermissionPreProcessing(Set<UserPermission> createdPermissions) {
            return createdPermissions.stream()
                    .map(userPermission -> shouldHavePermission(userPermission, OWNERS_AUTHORITIES, USERS_AUTHORITIES))
                    .collect(toSet());
        }
    }

    public static class NotebookWrapper extends EntityWrapper {

        private static final List<Authority> OWNERS_AUTHORITIES;

        private static final List<Authority> USERS_AUTHORITIES;

        static {
            OWNERS_AUTHORITIES = Arrays.asList(NOTEBOOK_READER, NOTEBOOK_CREATOR,
                    EXPERIMENT_READER, EXPERIMENT_CREATOR);
            USERS_AUTHORITIES = Arrays.asList(NOTEBOOK_READER, EXPERIMENT_READER,
                    EXPERIMENT_CREATOR);
        }

        private NotebookWrapper() {
            permissionsPreProcessing = NotebookWrapper::notebookPermissionPreProcessing;
            permissionsUpPreProcessing = ProjectWrapper::projectPermissionPreProcessing;
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

        private static NotebookWrapper parentForExperimentWrapper(Project project,
                                                                  Notebook notebook,
                                                                  ExperimentWrapper experimentWrapper
        ) {
            NotebookWrapper wrapper = new NotebookWrapper();
            wrapper.parent = ProjectWrapper.parentForNotebookWrapper(project, wrapper);
            wrapper.children = Collections.singletonList(experimentWrapper);
            wrapper.value = notebook;
            return wrapper;
        }

        private static Set<UserPermission> notebookPermissionPreProcessing(Set<UserPermission> createdPermissions) {
            return createdPermissions.stream()
                    .map(userPermission -> shouldHavePermission(userPermission, OWNERS_AUTHORITIES, USERS_AUTHORITIES))
                    .collect(toSet());
        }
    }

    public static class ExperimentWrapper extends EntityWrapper {

        private static final List<Authority> OWNERS_AUTHORITIES;

        private static final List<Authority> USERS_AUTHORITIES;

        static {
            USERS_AUTHORITIES = Arrays.asList(Authority.values());
            OWNERS_AUTHORITIES = Arrays.asList(EXPERIMENT_READER, EXPERIMENT_CREATOR);
        }

        private ExperimentWrapper() {
            permissionsPreProcessing = ExperimentWrapper::experimentsPermissionPreProcessing;
            permissionsUpPreProcessing = NotebookWrapper::notebookPermissionPreProcessing;
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
                            userPermission.getUser().getAuthorities().contains(EXPERIMENT_READER))
                    .map(userPermission -> shouldHavePermission(userPermission, OWNERS_AUTHORITIES, USERS_AUTHORITIES))
                    .collect(toSet());
        }
    }
}
