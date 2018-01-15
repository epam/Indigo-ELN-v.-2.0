package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.security.Authority;
import lombok.Getter;

import java.util.*;
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

    Set<UserPermission> addOrUpdatePermissionsUpForOneLevel(Set<UserPermission> createdPermissions) {
        Set<UserPermission> permissions = permissionsUpPreProcessing.apply(createdPermissions);
        HashSet<UserPermission> addedPermissions = new HashSet<>(permissions);

        for (UserPermission userPermission : permissions) {
            UserPermission userPermissionOuterEntity = PermissionUtil.findPermissionsByUserId(
                    parent.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                parent.getValue().getAccessList().add(userPermission);
            } else {
                if (PermissionUtil.canBeChangedFromThisLevel(
                        userPermissionOuterEntity.getPermissionCreationLevel(), permissionCreationLevel)) {
                    userPermissionOuterEntity.setPermissions(userPermission.getPermissions());
                } else {
                    addedPermissions.remove(userPermission);
                }
            }
        }
        return addedPermissions;
    }

    Set<UserPermission> removePermissionsUpForOneLevel(Set<UserPermission> removedPermissions) {
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

    /**
     * Check and downgrade if needed that user has enough authorities for userPermission.
     *
     * @param userPermission    current user permission
     * @param ownersAuthorities authorities needed to have owner role
     * @param usersAuthorities  authorities needed to have user role
     * @return the same {@link UserPermission} if user has enough authorities
     * and the new one with closest allowed permissions if he hasn't
     */
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

        /**
         * Create a {@link ProjectWrapper} witch contains only notebookWrapper as child.
         *
         * @param project         a value of wrapper
         * @param notebookWrapper a child for wrapper
         * @return a {@link ProjectWrapper} witch contains only notebookWrapper as child.
         */
        private static ProjectWrapper parentForNotebookWrapper(Project project, NotebookWrapper notebookWrapper) {
            ProjectWrapper wrapper = new ProjectWrapper();
            wrapper.children = Collections.singletonList(notebookWrapper);
            wrapper.value = project;
            wrapper.permissionCreationLevel = PermissionCreationLevel.PROJECT;
            return wrapper;
        }

        /**
         * Process permissions and downgrade ones if needed.
         *
         * @param createdPermissions applied permissions
         * @return correct applied permissions
         */
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
            USERS_AUTHORITIES = Arrays.asList(NOTEBOOK_READER, EXPERIMENT_READER, EXPERIMENT_CREATOR);
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

        /**
         * Create a {@link NotebookWrapper} with parent from parameter.
         *
         * @param parent   a parent for wrapper
         * @param notebook a value of wrapper
         * @return a {@link ProjectWrapper} witch contains only notebookWrapper as child.
         */
        private NotebookWrapper(ProjectWrapper parent, Notebook notebook) {
            this();
            this.parent = parent;
            this.children = notebook.getExperiments().stream()
                    .map(experiment -> new ExperimentWrapper(this, experiment))
                    .collect(toList());
            this.value = notebook;
        }

        /**
         * Create a {@link NotebookWrapper} witch contains only experimentWrapper as child.
         *
         * @param project           a parents value
         * @param notebook          a value of wrapper
         * @param experimentWrapper a child for wrapper
         * @return a {@link ProjectWrapper} witch contains only notebookWrapper as child.
         */
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

        /**
         * Process permissions and downgrade ones if needed.
         *
         * @param createdPermissions applied permissions
         * @return correct applied permissions
         */
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

        /**
         * Process permissions and downgrade ones if needed.
         *
         * @param createdPermissions applied permissions
         * @return correct applied permissions
         */
        private static Set<UserPermission> experimentsPermissionPreProcessing(Set<UserPermission> createdPermissions) {
            return createdPermissions.stream()
                    .filter(userPermission ->
                            userPermission.getUser().getAuthorities().contains(EXPERIMENT_READER)
                                    || userPermission.getUser().getAuthorities().contains(CONTENT_EDITOR))
                    .map(userPermission -> shouldHavePermission(userPermission, OWNERS_AUTHORITIES, USERS_AUTHORITIES))
                    .collect(toSet());
        }
    }
}
