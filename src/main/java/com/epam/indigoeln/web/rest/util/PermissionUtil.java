package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean isContentEditor(User user) {
        return user.getAuthorities().contains(Authority.CONTENT_EDITOR);
    }

    public static UserPermission findPermissionsByUserId(
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
        UserPermission userPermission = findPermissionsByUserId(accessList, user.getId());
        if (userPermission != null) {
            userPermission.setPermissions(permissions);
        } else {
            userPermission = new UserPermission(user, permissions);
            accessList.add(userPermission);
        }
    }

    public static boolean addUsersFromLowLevelToUp(Set<UserPermission> accessList,
                                                   UserPermission up, Set<String> permissions,
                                                   Set<User> usersWhichHasAlreadyExist) {
        if (!usersWhichHasAlreadyExist.contains(up.getUser())) {
            UserPermission userPermission = findPermissionsByUserId(accessList, up.getUser().getId());
            if (!UserPermission.OWNER.equals(up.getPermissionView())) {
                if (userPermission != null) {
                    Set<String> existingPermissions = userPermission.getPermissions();
                    if (existingPermissions == null) {
                        existingPermissions = new HashSet<>();
                        userPermission.setPermissions(existingPermissions);
                    }
                    return existingPermissions.addAll(permissions);
                } else {
                    userPermission = new UserPermission(up.getUser(), permissions);
                    accessList.add(userPermission);
                    return true;
                }
            } else {
                return accessList.add(up);
            }
        }
        return false;
    }

    /**
     * Import users from upper level (for example, when you create notebook, you need to add user from project)
     * @param accessList
     * @param userPermission
     * @param entityName
     */
    public static void importUsersFromUpperLevel(Set<UserPermission> accessList, UserPermission userPermission,
                                                 FirstEntityName entityName) {
        //Check if user is already in inner entity, then remove it and add for updating
        if (userPermission != null && userPermission.getPermissionView() != null
                && !FirstEntityName.firstEntityIsInner(userPermission.getFirstEntityName(),entityName)) {
            //It is does not work!
            accessList.removeIf(up -> up.getUser().getId().equals(userPermission.getUser().getId()));
            accessList.add(userPermission);
        }
    }

    public static void importUsersFromUpperLevel(Set<UserPermission> accessList, UserPermission userPermission) {
        if (userPermission != null && userPermission.getPermissionView() != null) {
            accessList.removeIf(up -> up.getUser().getId().equals(userPermission.getUser().getId()));
            accessList.add(userPermission);
        }
    }

    /**
     * When you change permission for user it update users in inner entities
     * @param entities
     * @param usersIds
     * @param upperEntity
     * @param <T>
     * @param <E>
     * @return
     */
    public static <T extends BasicModelObject, E extends BasicModelObject>
    List<T> updateInnerPermissionsLists(List<T> entities,
                                        Set<String> usersIds,
                                        E upperEntity) {
        if (entities != null) {
            for (T entity : entities) {
                Set<UserPermission> filtered = entity.getAccessList().stream()
                        .filter(up -> usersIds.contains(up.getUser().getId()))
                        .collect(Collectors.toSet());
                upperEntity.getAccessList().forEach(up -> importUsersFromUpperLevel(filtered, up));
                entity.setAccessList(filtered);
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

    //Update - add entity name for new users
    public static Set<UserPermission> updateFirstEntityNames(Set<UserPermission> accessList,
                                                             Set<UserPermission> newAccessList,
                                                             FirstEntityName firstEntityName){
        Set<User> users = accessList.stream()
                .map(UserPermission::getUser).collect(Collectors.toSet());
        Set<UserPermission> newUsers = newAccessList.stream()
                .filter(up -> !users.contains(up.getUser())).collect(Collectors.toSet());
        newAccessList.removeAll(newUsers);
        newAccessList.addAll(PermissionUtil.addFirstEntityName(newUsers, firstEntityName));
        return newAccessList;
    }

    //Add entity name
    public static Set<UserPermission> addFirstEntityName(Set<UserPermission> accessList, FirstEntityName firstEntityName){
        Set<UserPermission> res = new HashSet<>();
        accessList.forEach(userPermission -> userPermission.setFirstEntityName(firstEntityName));
        res.addAll(accessList);
        return res;
    }
}