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

    public static boolean hasPermissions(String userId,
                                         List<UserPermission> accessList,
                                         String permission) {
        // Check of UserPermission
        UserPermission userPermission = findPermissionsByUserId(accessList, userId);
        return userPermission != null && userPermission.hasPermission(permission);
    }

    public static boolean hasEditorAuthorityOrPermissions(User user,
                                         List<UserPermission> accessList,
                                         String permission) {
        return isContentEditor(user) || hasPermissions(user.getId(), accessList, permission);
    }

    public static boolean hasPermissions(String userId, List<UserPermission> parentAccessList,
                                         String parentPermission,
                                         List<UserPermission> childAccessList,
                                         String childPermission) {
        return hasPermissions(userId, parentAccessList, parentPermission) &&
                hasPermissions(userId, childAccessList, childPermission);
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