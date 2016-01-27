package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Authority;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;

import java.util.List;

public class PermissionUtil {

    public static boolean isAdmin(User user) {
        return user.getAuthorities().contains(Authority.ADMIN);
    }

    public static boolean hasAuthority(User user, String authority) {
        return user.getAuthorities().stream().anyMatch(auth -> auth.getName().equals(authority));
    }

    public static UserPermission findPermissionsByUserId(
            List<UserPermission> accessList, String userId) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUserId().equals(userId)) {
                return userPermission;
            }
        }
        return null;
    }

    public static boolean hasPermissions(User user,
                                         List<UserPermission> accessList,
                                         String permission) {
        if (isAdmin(user)) {
            return true;
        } else {
            // Check of UserPermission
            UserPermission userPermission = findPermissionsByUserId(
                    accessList, user.getId());
            if (userPermission != null && userPermission.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPermissions(User user, List<UserPermission> parentAccessList,
                                         String parentPermission,
                                         List<UserPermission> childAccessList,
                                         String childPermission) {
        return hasPermissions(user, parentAccessList, parentPermission) &&
                hasPermissions(user, childAccessList, childPermission);
    }


    /**
     * Adding of OWNER's permissions to Entity Access List for specified User, if it is absent
     */
    public static void addOwnerToAccessList(List<UserPermission> accessList, String userId) {
        UserPermission userPermission = findPermissionsByUserId(accessList, userId);
        if (userPermission != null) {
            userPermission.setPermissions(UserPermission.OWNER_PERMISSIONS);
        } else {
            userPermission = new UserPermission(userId, UserPermission.OWNER_PERMISSIONS);
            accessList.add(userPermission);
        }
    }
}