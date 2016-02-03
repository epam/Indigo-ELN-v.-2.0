package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.security.Authority;

import java.util.List;

public class PermissionUtil {

    public static boolean isContentEditor(User user) {
        return user.getAuthorities().contains(Authority.CONTENT_EDITOR);
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
        if (isContentEditor(user)) {
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