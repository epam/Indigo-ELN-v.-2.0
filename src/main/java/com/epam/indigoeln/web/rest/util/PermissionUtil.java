package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import org.apache.commons.lang3.StringUtils;
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

    public static UserPermission findFirstPermissionsByUserId(
            Set<UserPermission> accessList, String userId) {
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
        UserPermission userPermission = findFirstPermissionsByUserId(accessList, userId);
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
     * @param accessList Access list
     * @param user       User
     */
    public static void addOwnerToAccessList(Set<UserPermission> accessList, User user) {
        setUserPermissions(accessList, user, UserPermission.OWNER_PERMISSIONS);
    }

    /**
     * Adding Project Author as VIEWER to Experiment Access List if Experiment creator is another User.
     *
     * @param accessList        Access list
     * @param projectAuthor     Project's author
     * @param experimentCreator Experiment's creator
     */
    public static void addProjectAuthorToAccessList(Set<UserPermission> accessList,
                                                    User projectAuthor, User experimentCreator) {
        if (!StringUtils.equals(projectAuthor.getId(), experimentCreator.getId())) {
            setUserPermissions(accessList, projectAuthor, UserPermission.VIEWER_PERMISSIONS);
        }
    }

    private static void setUserPermissions(Set<UserPermission> accessList, User user, Set<String> permissions) {
        UserPermission userPermission = findFirstPermissionsByUserId(accessList, user.getId());
        if (userPermission != null) {
            userPermission.setPermissions(permissions);
        } else {
            userPermission = new UserPermission(user, permissions);
            accessList.add(userPermission);
        }
    }

    /**
     * Add new permission for up-level permissions.
     *
     * @param upLevelAccessList         up-level permissions
     * @param newUserPermission         permission to add to up-level permissions
     * @param permissions               if user does not have owner permissions in newUserPermission this permissions will be added
     * @param usersWhichHasAlreadyExist list of users those are already present in up-level permissions
     * @return if permission was added ({@code true}) on not ({@code false})
     */
    public static boolean addUsersFromLowLevelToUp(Set<UserPermission> upLevelAccessList,
                                                   UserPermission newUserPermission,
                                                   Set<String> permissions,
                                                   Set<User> usersWhichHasAlreadyExist) {
        if (!usersWhichHasAlreadyExist.contains(newUserPermission.getUser())) {
            if (!UserPermission.OWNER.equals(newUserPermission.getPermissionView())) {
                return addPermissionsForGeneralUserToUpLevelPermissions(
                        upLevelAccessList, newUserPermission.getUser(), permissions);
            } else {
                return upLevelAccessList.add(newUserPermission);
            }
        }
        return false;
    }

    private static boolean addPermissionsForGeneralUserToUpLevelPermissions(Set<UserPermission> upLevelAccessList, User user, Set<String> permissions) {
        UserPermission userPermission = findFirstPermissionsByUserId(upLevelAccessList, user.getId());
        if (userPermission != null) {
            Set<String> existingPermissions = userPermission.getPermissions();
            if (existingPermissions == null) {
                existingPermissions = new HashSet<>();
                userPermission.setPermissions(existingPermissions);
            }
            return existingPermissions.addAll(permissions);
        } else {
            upLevelAccessList.add(new UserPermission(user, permissions));
            return true;
        }
    }

    /**
     * Import users from upper level (for example, when you create notebook, you need to add user from project).
     *
     * @param accessList     current set of entities' permissions
     * @param userPermission permission to add
     * @param entityName     entity of origin permission
     */
    public static void importUsersFromUpperLevel(Set<UserPermission> accessList, UserPermission userPermission,
                                                 FirstEntityName entityName) {
        //Check if user is already in inner entity, then remove it and add for updating
        if (userPermission != null && userPermission.getPermissionView() != null
                && !FirstEntityName.firstEntityIsInner(userPermission.getFirstEntityName(), entityName)) {
            //It is does not work!
            accessList.removeIf(up -> equalsByUserId(userPermission, up));
            accessList.add(userPermission);
        }
    }

    public static void addUsersFromUpperLevel(Set<UserPermission> accessList,
                                              Set<UserPermission> upperLevelUserPermissions,
                                              FirstEntityName upperLevelFirstEntityName
    ) {
        upperLevelUserPermissions.forEach(up -> {
            if (canBeAddedFromUpperLevel(upperLevelFirstEntityName, up)) {
                accessList.add(up);
            }
        });
    }

    private static boolean canBeAddedFromUpperLevel(FirstEntityName upperLevelFirstEntityName, UserPermission up) {
        return up != null &&
                Comparator.comparing(FirstEntityName::ordinal)
                        .compare(upperLevelFirstEntityName, up.getFirstEntityName()) >= 0;
    }

    /**
     * Change permission for user in {@code upperEntity} provides update users permissions in inner {@code entities}.
     *
     * @param entities        inner entities of {@code upperEntity}
     * @param updatesUsersIds identity for updated users
     * @param upperEntity     entity that was updated
     * @param <T>             could be {@link com.epam.indigoeln.core.model.Experiment}
     *                        or {@link com.epam.indigoeln.core.model.Notebook}
     * @param <E>             could be {@link com.epam.indigoeln.core.model.Notebook}
     *                        or {@link com.epam.indigoeln.core.model.Project}
     * @return list of entities with updated permissions
     */
    public static <T extends BasicModelObject, E extends BasicModelObject>
    List<T> updateInnerPermissionsLists(List<T> entities,
                                        Set<String> updatesUsersIds,
                                        E upperEntity
    ) {
        if (entities != null) {
            for (T entity : entities) {
                Set<UserPermission> updatedUsersPermissions = entity.getAccessList().stream()
                        .filter(userPermission -> updatesUsersIds.contains(userPermission.getUser().getId()))
                        .collect(toSet());
                entity.getAccessList().removeAll(updatedUsersPermissions);
                upperEntity.getAccessList().forEach(userPermission -> {
                    if (userPermission != null && userPermission.getPermissionView() != null) {
                        updatedUsersPermissions.removeIf(up -> equalsByUserId(userPermission, up));
                        updatedUsersPermissions.add(userPermission);
                    }
                });
                entity.getAccessList().addAll(updatedUsersPermissions);
            }
        }
        return entities;
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


    /**
     * Set firstEntityName for users from newAccessList witch don't present in accessList.
     *
     * @param accessList      current permission for entity
     * @param newAccessList   list with old and new permissions
     * @param firstEntityName name of origin entity of given permission
     * @return list of new users permissions with sets firstEntityName
     */
    public static Set<UserPermission> updateFirstEntityNames(Set<UserPermission> accessList,
                                                             Set<UserPermission> newAccessList,
                                                             FirstEntityName firstEntityName) {

        accessList.removeIf(userPermission ->
                newAccessList.stream().anyMatch(newUserPermission ->
                        equalsByUserId(userPermission, newUserPermission)
                                && !newUserPermission.getPermissions().equals(userPermission.getPermissions()))
                        || newAccessList.stream().noneMatch(newUserPermission ->
                        equalsByUserId(userPermission, newUserPermission))
        );
        newAccessList.removeIf(newUserPermission ->
                accessList.stream().anyMatch(oldUserPermission ->
                        equalsByUserId(newUserPermission, oldUserPermission)));
        return PermissionUtil.addFirstEntityName(newAccessList, firstEntityName);
    }

    private static boolean equalsByUserId(UserPermission oldPermission, UserPermission newPermission) {
        return Objects.equals(newPermission.getUser().getId(), oldPermission.getUser().getId());
    }

    //Add entity name
    public static Set<UserPermission> addFirstEntityName(Set<UserPermission> accessList, FirstEntityName firstEntityName) {
        accessList.forEach(userPermission -> userPermission.setFirstEntityName(firstEntityName));
        return new HashSet<>(accessList);
    }

    public static Pair<Boolean, Boolean> changeExperimentPermissions(Project project, Notebook notebook, Experiment experiment, Set<UserPermission> newUserPermissions) {

        boolean notebookHadChanged = false;
        boolean projectHadChanged = false;

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            addPermissionsDown(EntityWrapper.of(experiment), newUserPermissions);

            notebookHadChanged = addPermissionsUp(EntityWrapper.of(notebook), EntityWrapper.of(experiment), newUserPermissions);

            projectHadChanged = addPermissionsUp(EntityWrapper.of(experiment), EntityWrapper.of(notebook), newUserPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            updatePermissionsDown(EntityWrapper.of(experiment), newUserPermissions);

            Set<UserPermission> permissionsUpdatedInNotebook = updatePermissionsUp(EntityWrapper.of(notebook), EntityWrapper.of(experiment), newUserPermissions);

            notebookHadChanged |= !permissionsUpdatedInNotebook.isEmpty();

            projectHadChanged |= !updatePermissionsUp(EntityWrapper.of(project), EntityWrapper.of(notebook), permissionsUpdatedInNotebook).isEmpty();

        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            removePermissionsDown(EntityWrapper.of(experiment), removedPermissions);

            Set<UserPermission> removedFromNotebook = removePermissionsUp(EntityWrapper.of(notebook), EntityWrapper.of(experiment), removedPermissions);

            notebookHadChanged |= !removedFromNotebook.isEmpty();

            projectHadChanged |= !removePermissionsUp(EntityWrapper.of(project), EntityWrapper.of(notebook), removedFromNotebook).isEmpty();
        }

        return Pair.of(notebookHadChanged, projectHadChanged);
    }

    public static boolean changeNotebookPermissions(Project project, Notebook notebook, Set<UserPermission> newUserPermissions) {

        boolean projectHadChanged = false;

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            EntityWrapper innerEntity = EntityWrapper.of(notebook);

            projectHadChanged = addPermissionsUp(EntityWrapper.of(project), innerEntity, newUserPermissions);
            addPermissionsDown(innerEntity, newUserPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            projectHadChanged |= updatePermissionsUpAndDown(EntityWrapper.of(project), EntityWrapper.of(notebook), newUserPermissions);
        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            projectHadChanged |= removePermissionsUpAndDown(EntityWrapper.of(project), EntityWrapper.of(notebook), removedPermissions);
        }
        return projectHadChanged;
    }

    public static void changeProjectPermissions(Project project, Set<UserPermission> newUserPermissions) {

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            addPermissionsDown(EntityWrapper.of(project), addFirstEntityName(createdPermissions, FirstEntityName.PROJECT));
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            updatePermissionsDown(EntityWrapper.of(project), addFirstEntityName(updatedPermissions, FirstEntityName.PROJECT));
        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            removePermissionsDown(EntityWrapper.of(project), removedPermissions);
        }
    }

    private static void removePermissionsDown(EntityWrapper entity, Set<UserPermission> removedPermissions) {
        entity.getChildren().forEach(child -> removePermissionsDown(child, removedPermissions));

        entity.getValue().getAccessList().removeIf(oldPermission -> removedPermissions.stream()
                .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));
    }

    private static void updatePermissionsDown(EntityWrapper entity, Set<UserPermission> updatedPermissions) {

        entity.getChildren().forEach(child -> updatePermissionsDown(child, updatedPermissions));

        entity.getValue().getAccessList().removeIf(oldPermission -> updatedPermissions.stream()
                .anyMatch(updatedPermission -> equalsByUserId(oldPermission, updatedPermission)));

        entity.getValue().getAccessList().addAll(updatedPermissions);
    }

    private static void addPermissionsDown(EntityWrapper entity, Set<UserPermission> createdPermissions) {

        entity.getChildren().forEach(children -> addPermissionsDown(children, createdPermissions));

        entity.getValue().getAccessList().addAll(createdPermissions);
    }

    private static boolean removePermissionsUpAndDown(EntityWrapper outerEntity,
                                                      EntityWrapper innerEntity,
                                                      Set<UserPermission> removedPermissions
    ) {
        removePermissionsDown(innerEntity, removedPermissions);

        Set<UserPermission> removed = removePermissionsUp(outerEntity, innerEntity, removedPermissions);

        return !removed.isEmpty();
    }

    private static Set<UserPermission> removePermissionsUp(EntityWrapper outerEntity,
                                                           EntityWrapper innerEntity,
                                                           Set<UserPermission> removedPermissions
    ) {
        Set<UserPermission> canBeRemovedFromInnerEntity = outerEntity.getValue().getAccessList().stream()
                .filter(userPermission -> canBeChangedFromThisLevel(userPermission.getFirstEntityName(), innerEntity.getFirstEntityName()))
                .collect(toSet());

        Set<UserPermission> containsInProjectsNotebooks = outerEntity.getChildren().stream()
                .flatMap(notebook1 -> notebook1.getValue().getAccessList().stream())
                .collect(toSet());

        removedPermissions.removeIf(removingPermission ->
                containsInProjectsNotebooks.stream()
                        .anyMatch(cantBeRemoved ->
                                equalsByUserId(cantBeRemoved, removingPermission))
                        || canBeRemovedFromInnerEntity.stream()
                        .noneMatch(canBeRemoved ->
                                equalsByUserId(canBeRemoved, removingPermission))
        );

        removePermissionsDown(outerEntity, removedPermissions);

        return removedPermissions;
    }

    private static boolean addPermissionsUp(EntityWrapper outerEntity, EntityWrapper innerEntity, Set<UserPermission> createdPermissions) {
        boolean outerEntityHadChanged = false;

        for (UserPermission userPermission : createdPermissions) {
            UserPermission userPermissionOuterEntity = findFirstPermissionsByUserId(outerEntity.getValue().getAccessList(), userPermission.getUser().getId());
            if (userPermissionOuterEntity == null) {
                outerEntity.getValue().getAccessList().add(UserPermission.OWNER.equals(userPermission.getPermissionView())
                        ? userPermission
                        : userPermission.setPermissions(UserPermission.VIEWER_PERMISSIONS));
                outerEntityHadChanged = true;
            } else {
                if (canBeChangedFromThisLevel(userPermissionOuterEntity.getFirstEntityName(), innerEntity.getFirstEntityName())) {
                    userPermissionOuterEntity.setPermissions(UserPermission.OWNER.equals(userPermission.getPermissionView())
                            ? userPermission.getPermissions()
                            : UserPermission.VIEWER_PERMISSIONS);
                    outerEntityHadChanged = true;
                }
            }
        }

        return outerEntityHadChanged;
    }

    private static boolean updatePermissionsUpAndDown(EntityWrapper outerEntity, EntityWrapper
            innerEntity, Set<UserPermission> updatedPermissions) {

        updatePermissionsDown(innerEntity, updatedPermissions);

        return !updatePermissionsUp(outerEntity, innerEntity, updatedPermissions).isEmpty();
    }

    private static Set<UserPermission> updatePermissionsUp(EntityWrapper outerEntity, EntityWrapper innerEntity, Set<UserPermission> updatedPermissions) {
        Set<UserPermission> updated = new HashSet<>();

        for (UserPermission permission : updatedPermissions) {
            UserPermission userPermissionOuterEntity = findFirstPermissionsByUserId(outerEntity.getValue().getAccessList(), permission.getUser().getId());
            if (userPermissionOuterEntity != null &&
                    canBeChangedFromThisLevel(userPermissionOuterEntity.getFirstEntityName(), innerEntity.getFirstEntityName())) {
                userPermissionOuterEntity.setPermissions(UserPermission.OWNER.equals(permission.getPermissionView())
                        ? permission.getPermissions()
                        : UserPermission.VIEWER_PERMISSIONS);

                updated.add(userPermissionOuterEntity);
            }
        }

        return updated;
    }

    private static boolean canBeChangedFromThisLevel(FirstEntityName firstEntityName, FirstEntityName innerEntityFirstEntityName) {
        return firstEntityName != null
                && innerEntityFirstEntityName != null
                && Comparator.comparing(FirstEntityName::ordinal)
                .compare(firstEntityName, innerEntityFirstEntityName) >= 0;
    }


    private abstract static class EntityWrapper {

        public abstract List<EntityWrapper> getChildren();

        public abstract BasicModelObject getValue();

        public abstract FirstEntityName getFirstEntityName();

        public static EntityWrapper of(Project project) {
            return new EntityWrapper() {
                private List<EntityWrapper> children = project.getNotebooks().stream()
                        .map(EntityWrapper::of)
                        .collect(toList());

                @Override
                public List<EntityWrapper> getChildren() {
                    return children;
                }

                @Override
                public BasicModelObject getValue() {
                    return project;
                }

                @Override
                public FirstEntityName getFirstEntityName() {
                    return FirstEntityName.PROJECT;
                }
            };
        }

        public static EntityWrapper of(Notebook notebook) {
            return new EntityWrapper() {
                private List<EntityWrapper> children = notebook.getExperiments().stream()
                        .map(EntityWrapper::of)
                        .collect(toList());

                @Override
                public List<EntityWrapper> getChildren() {
                    return children;
                }

                @Override
                public BasicModelObject getValue() {
                    return notebook;
                }

                @Override
                public FirstEntityName getFirstEntityName() {
                    return FirstEntityName.NOTEBOOK;
                }
            };
        }

        public static EntityWrapper of(Experiment experiment) {
            return new EntityWrapper() {
                @Override
                public List<EntityWrapper> getChildren() {
                    return Collections.emptyList();
                }

                @Override
                public BasicModelObject getValue() {
                    return experiment;
                }

                @Override
                public FirstEntityName getFirstEntityName() {
                    return FirstEntityName.EXPERIMENT;
                }
            };
        }
    }
}