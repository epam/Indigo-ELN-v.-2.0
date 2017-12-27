package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean isContentEditor(User user) {
        return user.getAuthorities().contains(Authority.CONTENT_EDITOR);
    }

    public static UserPermission findPermissionsByUserId(Set<UserPermission> accessList,
                                                         String userId
    ) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUser().getId().equals(userId)) {
                return userPermission;
            }
        }
        return null;
    }

    public static boolean hasPermissions(String userId,
                                         Set<UserPermission> accessList,
                                         String permission) {
        // Check of UserPermission
        UserPermission userPermission = findPermissionsByUserId(accessList, userId);
        return userPermission != null && userPermission.hasPermission(permission);
    }

    public static boolean hasEditorAuthorityOrPermissions(User user,
                                                          Set<UserPermission> accessList,
                                                          String permission) {
        return isContentEditor(user) || hasPermissions(user.getId(), accessList, permission);
    }

    public static boolean hasPermissions(String userId, Set<UserPermission> parentAccessList,
                                         String parentPermission,
                                         Set<UserPermission> childAccessList,
                                         String childPermission) {
        return hasPermissions(userId, parentAccessList, parentPermission)
                && hasPermissions(userId, childAccessList, childPermission);
    }

    /**
     * Adding of OWNER's permissions to Entity Access List for specified User, if it is absent.
     *
     * @param accessList              Access list
     * @param user                    User
     * @param permissionCreationLevel
     */
    public static void addOwnerToAccessList(Set<UserPermission> accessList,
                                            User user,
                                            PermissionCreationLevel permissionCreationLevel
    ) {
        UserPermission userPermission = findPermissionsByUserId(accessList, user.getId());
        if (userPermission != null) {
            userPermission.setPermissions(UserPermission.OWNER_PERMISSIONS);
            userPermission.setPermissionCreationLevel(permissionCreationLevel);
        } else {
            userPermission = new UserPermission(user, UserPermission.OWNER_PERMISSIONS);
            userPermission.setPermissionCreationLevel(permissionCreationLevel);
            accessList.add(userPermission);
        }
    }

    public static void addUsersFromUpperLevel(Set<UserPermission> accessList,
                                              Set<UserPermission> upperLevelUserPermissions,
                                              PermissionCreationLevel upperPermissionsLevel
    ) {
        upperLevelUserPermissions.forEach(up -> {
            if (canBeAddedFromUpperLevel(upperPermissionsLevel, up)) {
                accessList.add(up);
            }
        });
    }

    private static boolean canBeAddedFromUpperLevel(PermissionCreationLevel upperPermissionsLevel, UserPermission up) {
        return up != null
                && up.getPermissionCreationLevel() != null
                && Comparator.comparing(PermissionCreationLevel::ordinal)
                .compare(upperPermissionsLevel, up.getPermissionCreationLevel()) >= 0;
    }

    public static void checkCorrectnessOfAccessList(UserRepository userRepository,
                                                    Set<UserPermission> accessList) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUser() == null) {
                throw PermissionIncorrectException.createWithoutUserId();
            }

            User userFromDB = userRepository.findOne(userPermission.getUser().getId());
            if (userFromDB == null) {
                throw EntityNotFoundException.createWithUserId(userPermission.getUser().getId());
            }
            userPermission.setUser(userFromDB);

            // check correctness of permissions
            for (String permission : userPermission.getPermissions()) {
                if (!UserPermission.ALL_PERMISSIONS.contains(permission)) {
                    throw PermissionIncorrectException.createWithUserIdAndPermission(
                            userPermission.getUser().getId(), permission);
                }
            }

            if (userPermission.getUser().getAuthorities().contains(Authority.CONTENT_EDITOR)
                    && !UserPermission.OWNER.equals(userPermission.getPermissionView())) {
                throw PermissionIncorrectException
                        .createWithUserIdAndPermission(userPermission.getUser().getId(),
                                userPermission.getPermissionView());
            }
        }
    }


    private static boolean equalsByUserId(UserPermission oldPermission, UserPermission newPermission) {
        return Objects.equals(newPermission.getUser().getId(), oldPermission.getUser().getId());
    }

    public static Set<UserPermission> addFirstEntityName(Set<UserPermission> accessList,
                                                         PermissionCreationLevel permissionCreationLevel
    ) {
        accessList.forEach(userPermission -> userPermission.setPermissionCreationLevel(permissionCreationLevel));
        return new HashSet<>(accessList);
    }

    public static Pair<Boolean, Boolean> changeExperimentPermissions(Project project,
                                                                     Notebook notebook,
                                                                     Experiment experiment,
                                                                     Set<UserPermission> newUserPermissions
    ) {
        boolean notebookHadChanged = false;
        boolean projectHadChanged = false;

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> experiment.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.EXPERIMENT))
                .collect(toSet());

        EntityWrapper wrappedExperiment = new EntityWrapper.ExperimentWrapper(experiment);
        EntityWrapper wrappedNotebook = new EntityWrapper.NotebookWrapper(notebook);
        EntityWrapper wrappedProject = new EntityWrapper.ProjectWrapper(project);

        if (!createdPermissions.isEmpty()) {
            addPermissionsDown(wrappedExperiment, createdPermissions);

            Set<UserPermission> addedToNotebook = addPermissionsUp(
                    wrappedNotebook, wrappedExperiment, createdPermissions);
            notebookHadChanged = !addedToNotebook.isEmpty();

            projectHadChanged = !addPermissionsUp(
                    wrappedProject, wrappedNotebook, addedToNotebook)
                    .isEmpty();
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> experiment.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.EXPERIMENT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            updatePermissionsDown(wrappedExperiment, updatedPermissions);

            Set<UserPermission> permissionsUpdatedInNotebook = updatePermissionsUp(
                    wrappedNotebook, wrappedExperiment, updatedPermissions);

            notebookHadChanged |= !permissionsUpdatedInNotebook.isEmpty();

            projectHadChanged |= !updatePermissionsUp(
                    wrappedProject, wrappedNotebook, permissionsUpdatedInNotebook)
                    .isEmpty();

        }

        Set<UserPermission> removedPermissions = experiment.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            removePermissionsDown(wrappedExperiment, removedPermissions);

            Set<UserPermission> removedFromNotebook = removePermissionsUp(
                    wrappedNotebook, wrappedExperiment, removedPermissions);

            notebookHadChanged |= !removedFromNotebook.isEmpty();

            projectHadChanged |= !removePermissionsUp(
                    wrappedProject, wrappedNotebook, removedFromNotebook)
                    .isEmpty();
        }

        return Pair.of(notebookHadChanged, projectHadChanged);
    }

    public static boolean changeNotebookPermissions(Project project,
                                                    Notebook notebook,
                                                    Set<UserPermission> newUserPermissions
    ) {
        boolean projectHadChanged = false;

        EntityWrapper wrappedNotebook = new EntityWrapper.NotebookWrapper(notebook);

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {

            projectHadChanged = !addPermissionsUp(
                    new EntityWrapper.ProjectWrapper(project), wrappedNotebook, createdPermissions)
                    .isEmpty();

            addPermissionsDown(wrappedNotebook, createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            projectHadChanged |= updatePermissionsUpAndDown(
                    new EntityWrapper.ProjectWrapper(project), wrappedNotebook, updatedPermissions);
        }

        Set<UserPermission> removedPermissions = notebook.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            removePermissionsDown(wrappedNotebook, removedPermissions);

            Set<UserPermission> removed = removePermissionsUp(new EntityWrapper.ProjectWrapper(project), wrappedNotebook, removedPermissions);

            projectHadChanged |= !removed.isEmpty();
        }
        return projectHadChanged;
    }

    public static void changeProjectPermissions(Project project, Set<UserPermission> newUserPermissions) {

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            addPermissionsDown(new EntityWrapper.ProjectWrapper(project), createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            updatePermissionsDown(new EntityWrapper.ProjectWrapper(project), updatedPermissions);
        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            removePermissionsDown(new EntityWrapper.ProjectWrapper(project), removedPermissions);
        }
    }

    private static void addPermissionsDown(EntityWrapper entity, Set<UserPermission> createdPermissions) {

        entity.getChildren().forEach(children -> addPermissionsDown(children, createdPermissions));

        entity.getValue().getAccessList().addAll(createdPermissions);
    }

    private static Set<UserPermission> addPermissionsUp(EntityWrapper outerEntity,
                                                        EntityWrapper innerEntity,
                                                        Set<UserPermission> createdPermissions
    ) {
        Set<UserPermission> added = new HashSet<>();

        for (UserPermission userPermission : createdPermissions) {
            UserPermission userPermissionOuterEntity = findPermissionsByUserId(
                    outerEntity.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                outerEntity.getValue().getAccessList().add(
                        UserPermission.OWNER.equals(userPermission.getPermissionView())
                                ? userPermission
                                : userPermission.setPermissions(UserPermission.VIEWER_PERMISSIONS));
                added.add(userPermission);
            } else {
                changePermission(innerEntity, userPermission, userPermissionOuterEntity, added);
            }
        }

        return added;
    }

    private static void updatePermissionsDown(EntityWrapper entity, Set<UserPermission> updatedPermissions) {

        entity.getChildren().forEach(child -> updatePermissionsDown(child, updatedPermissions));

        entity.getValue().getAccessList().removeIf(oldPermission ->
                updatedPermissions.stream()
                        .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));

        entity.getValue().getAccessList().addAll(updatedPermissions);
    }

    private static Set<UserPermission> updatePermissionsUp(EntityWrapper outerEntity,
                                                           EntityWrapper innerEntity,
                                                           Set<UserPermission> updatedPermissions
    ) {
        Set<UserPermission> updated = new HashSet<>();

        for (UserPermission permission : updatedPermissions) {
            UserPermission userPermissionOuterEntity = findPermissionsByUserId(
                    outerEntity.getValue().getAccessList(), permission.getUser().getId());
            if (userPermissionOuterEntity != null) {
                changePermission(innerEntity, permission, userPermissionOuterEntity, updated);
            }
        }
        return updated;
    }

    private static void removePermissionsDown(EntityWrapper entity, Set<UserPermission> removedPermissions) {
        entity.getChildren().forEach(child -> removePermissionsDown(child, removedPermissions));

        entity.getValue().getAccessList().removeIf(oldPermission -> removedPermissions.stream()
                .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));
    }

    private static Set<UserPermission> removePermissionsUp(EntityWrapper outerEntity,
                                                           EntityWrapper innerEntity,
                                                           Set<UserPermission> removedPermissions
    ) {
        Set<UserPermission> canBeRemovedFromInnerEntity = outerEntity.getValue().getAccessList().stream()
                .filter(userPermission -> canBeChangedFromThisLevel(
                        userPermission.getPermissionCreationLevel(), innerEntity.getPermissionCreationLevel()))
                .collect(toSet());

        Set<UserPermission> containsInProjectsNotebooks = outerEntity.getChildren().stream()
                .filter(child -> !child.getValue().getId().equals(innerEntity.getValue().getId()))
                .flatMap(child -> child.getValue().getAccessList().stream())
                .collect(toSet());

        removedPermissions.removeIf(removingPermission -> userPresentInInnerEntitiesOrCantBeRemovedFromInnerEntity(
                canBeRemovedFromInnerEntity, containsInProjectsNotebooks, removingPermission));

        removePermissionsDown(outerEntity, removedPermissions);

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

    private static void changePermission(EntityWrapper innerEntity,
                                         UserPermission permission,
                                         UserPermission userPermissionOuterEntity,
                                         Set<UserPermission> updated //updating aggregation
    ) {
        if (canBeChangedFromThisLevel(
                userPermissionOuterEntity.getPermissionCreationLevel(), innerEntity.getPermissionCreationLevel())) {
            userPermissionOuterEntity.setPermissions(
                    UserPermission.OWNER.equals(permission.getPermissionView())
                            ? permission.getPermissions()
                            : UserPermission.VIEWER_PERMISSIONS);

            updated.add(userPermissionOuterEntity);
        }
    }

    private static boolean updatePermissionsUpAndDown(EntityWrapper outerEntity,
                                                      EntityWrapper innerEntity,
                                                      Set<UserPermission> updatedPermissions
    ) {
        updatePermissionsDown(innerEntity, updatedPermissions);

        return !updatePermissionsUp(outerEntity, innerEntity, updatedPermissions).isEmpty();
    }

    private static boolean canBeChangedFromThisLevel(PermissionCreationLevel changingPermissionLevel,
                                                     PermissionCreationLevel currentLevel
    ) {
        return changingPermissionLevel != null
                && currentLevel != null
                && Comparator.comparing(PermissionCreationLevel::ordinal)
                .compare(changingPermissionLevel, currentLevel) >= 0;
    }


    @Getter
    private abstract static class EntityWrapper {

        protected List<EntityWrapper> children;

        protected BasicModelObject value;

        protected PermissionCreationLevel permissionCreationLevel;

        private static class ProjectWrapper extends EntityWrapper {
            ProjectWrapper(Project project) {
                super();
                this.children = project.getNotebooks().stream()
                        .map(NotebookWrapper::new)
                        .collect(toList());
                this.value = project;
                this.permissionCreationLevel = PermissionCreationLevel.PROJECT;
            }
        }

        private static class NotebookWrapper extends EntityWrapper {
            NotebookWrapper(Notebook notebook) {
                this.children = notebook.getExperiments().stream()
                        .map(ExperimentWrapper::new)
                        .collect(toList());
                this.value = notebook;
                this.permissionCreationLevel = PermissionCreationLevel.NOTEBOOK;
            }
        }

        private static class ExperimentWrapper extends EntityWrapper {
            ExperimentWrapper(Experiment experiment) {
                this.value = experiment;
                this.children = Collections.emptyList();
                this.permissionCreationLevel = PermissionCreationLevel.EXPERIMENT;
            }
        }
    }
}