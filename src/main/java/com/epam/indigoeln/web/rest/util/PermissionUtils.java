package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Authority;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PermissionUtils {

    public static boolean isAdmin(User user) {
        return user.getAuthorities().contains(Authority.ADMIN);
    }

    public static @Nullable UserPermission findPermissionsByUserId(
            @NotNull List<UserPermission> accessList, @NotNull String userId) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUserId().equals(userId)) {
                return userPermission;
            }
        }
        return null;
    }

    public static boolean hasPermissions(@NotNull User user,
                                         @NotNull List<UserPermission> accessList,
                                         @NotNull String permission) {
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

    public static boolean hasPermissions(@NotNull User user, @NotNull List<UserPermission> parentAccessList,
                                         @NotNull String parentPermission,
                                         @NotNull List<UserPermission> childAccessList,
                                         @NotNull String childPermission) {
        return hasPermissions(user, parentAccessList, parentPermission) &&
                hasPermissions(user, childAccessList, childPermission);
    }


    /**
     * Adding of OWNER's permissions to Entity Access List for specified User, if it is absent
     */
    public static void addOwnerToAccessList(@NotNull List<UserPermission> accessList, @NotNull String userId) {
        UserPermission userPermission = findPermissionsByUserId(accessList, userId);
        if (userPermission != null) {
            userPermission.setPermissions(UserPermission.OWNER_PERMISSIONS);
        } else {
            userPermission = new UserPermission(userId, UserPermission.OWNER_PERMISSIONS);
            accessList.add(userPermission);
        }
    }
}