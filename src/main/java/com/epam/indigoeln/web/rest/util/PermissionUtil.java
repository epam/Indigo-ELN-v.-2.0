package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.PermissionCreationLevel;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import com.epam.indigoeln.core.service.user.UserService;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.NOTEBOOK;
import static com.epam.indigoeln.core.model.UserPermission.*;
import static java.util.stream.Collectors.toSet;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean isContentEditor(User user) {
        return user != null &&
                user.getAuthorities().contains(Authority.CONTENT_EDITOR);
    }

    /**
     * Search for {@link UserPermission} for user with userId in accessList.
     *
     * @param accessList access list of some resource
     * @param userId     identify of user whose permission on the entity should be founded
     * @return {@code null} if there is no permissions for user with userId in accessList
     * or {@link UserPermission} from accessList for this user
     */
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
     * @param permissionCreationLevel level of permission adding
     */
    public static void addOwnerToAccessList(Set<UserPermission> accessList,
                                            User user,
                                            PermissionCreationLevel permissionCreationLevel
    ) {
        accessList.removeIf(up -> up.getUser().getId().equals(user.getId()));
        UserPermission ownerPermission = new UserPermission(user, OWNER_PERMISSIONS, permissionCreationLevel, false);
        accessList.add(ownerPermission);
    }

    /**
     * Add permission from upper level permissions to current permissions.
     *
     * @param accessList                current entity permissions
     * @param upperLevelUserPermissions permissions of upper level entity
     * @param upperPermissionsLevel     upper entity permission level
     */
    public static void addUsersFromUpperLevel(Set<UserPermission> accessList,
                                              Set<UserPermission> upperLevelUserPermissions,
                                              PermissionCreationLevel upperPermissionsLevel
    ) {
        upperLevelUserPermissions.forEach(up -> {
            if (canBeAddedFromUpperLevel(upperPermissionsLevel, up) && !hasUser(accessList, up.getUser())) {
                accessList.add(new UserPermission(
                        up.getUser(),
                        (upperPermissionsLevel.equals(NOTEBOOK) && up.getPermissionView().equals(USER)) ?
                                VIEWER_PERMISSIONS : up.getPermissions(),
                        up.getPermissionCreationLevel(),
                        up.isRemovable()));
            }
        });
    }

    /**
     * Check if permission can be added from some level.
     *
     * @param upperPermissionsLevel Upper permission level
     * @param userPermission        User permission
     * @return {@code true} if {@code upperPermissionsLevel} is lower or equal to {@code userPermission} creation level.
     */
    private static boolean canBeAddedFromUpperLevel(PermissionCreationLevel upperPermissionsLevel,
                                                    UserPermission userPermission
    ) {
        return userPermission != null
                && canBeChangedFromThisLevel(upperPermissionsLevel, userPermission.getPermissionCreationLevel());
    }

    public static void checkCorrectnessOfAccessList(UserService userService,
                                                    Set<UserPermission> accessList) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUser() == null) {
                throw PermissionIncorrectException.createWithoutUserId();
            }

            User userFromDB = userService.getUserById(userPermission.getUser().getId());
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
        }
    }

    public static boolean hasUser(Set<UserPermission> removingPermissions, UserPermission userPermission) {
        return removingPermissions.stream()
                .anyMatch(removingPermission -> equalsByUserId(removingPermission, userPermission));
    }

    public static boolean hasUser(Set<UserPermission> accessList, User user) {
        return accessList.stream()
                .anyMatch(removingPermission -> Objects.equals(user.getId(), removingPermission.getUser().getId()));
    }

    /**
     * Check if these permissions are for the same user.
     *
     * @param oldPermission Old permission
     * @param newPermission New permission
     * @return {@code true} if permissions are for the same user
     */
    public static boolean equalsByUserId(UserPermission oldPermission, UserPermission newPermission) {
        return Objects.equals(newPermission.getUser().getId(), oldPermission.getUser().getId());
    }

    /**
     * Check if permission from current level can change changing permission.
     *
     * @param changingPermissionLevel Changing permission level
     * @param currentLevel            Current level
     * @return @code true} if entities from {@code changingPermissionLevel} can be changed
     * by permissions of {@code currentLevel}
     */
    public static boolean canBeChangedFromThisLevel(PermissionCreationLevel changingPermissionLevel,
                                                    PermissionCreationLevel currentLevel
    ) {
        return changingPermissionLevel != null
                && currentLevel != null
                && Comparator.comparing(PermissionCreationLevel::ordinal)
                .compare(changingPermissionLevel, currentLevel) >= 0;
    }

    /**
     * Compare actual entity accessList and newUserPermissions and returns permissions that should be removed from accessList.
     *
     * @param entity             modifying entity
     * @param newUserPermissions applied permission list
     * @return permissions that should be removed from accessList
     */
    public static Set<UserPermission> getRemovedPermissions(BasicModelObject entity,
                                                            Set<UserPermission> newUserPermissions) {
        return entity.getAccessList().stream()
                .filter(UserPermission::isRemovable)
                .filter(oldPermission ->
                        newUserPermissions.stream()
                                .noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());
    }

    /**
     * Compare actual entity accessList and newUserPermissions and returns updated permissions.
     * <p>
     * This method also sets creationLevel as permissionCreationLevel for updated permission.
     *
     * @param entity             modifying entity
     * @param newUserPermissions applied permission list
     * @param creationLevel      level of applying
     * @return updated permissions
     */
    public static Set<UserPermission> getUpdatedPermissions(BasicModelObject entity,
                                                            Set<UserPermission> newUserPermissions,
                                                            PermissionCreationLevel creationLevel
    ) {
        return newUserPermissions.stream()
                .filter(newPermission -> entity.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(creationLevel))
                .collect(toSet());
    }

    /**
     * Compare actual entity accessList and newUserPermissions and returns new permissions.
     *
     * @param entity             modifying entity
     * @param newUserPermissions applied permission list
     * @param creationLevel      level of applying
     * @return new permissions
     */
    public static Set<UserPermission> getCreatedPermission(BasicModelObject entity,
                                                           Set<UserPermission> newUserPermissions,
                                                           PermissionCreationLevel creationLevel
    ) {
        return newUserPermissions.stream()
                .filter(newPermission -> entity.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(creationLevel))
                .collect(toSet());
    }
}