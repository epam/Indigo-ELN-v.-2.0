package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that permissions are incorrect.
 */
public final class PermissionIncorrectException extends CustomParametrizedException {

    /**
     * Creates instance of PermissionIncorrectException.
     *
     * @param message Exception message
     * @param params  Exception parameters
     */
    private PermissionIncorrectException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of PermissionIncorrectException if user permission is incorrect.
     *
     * @param userId     User's id
     * @param permission User's permission
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithUserIdAndPermission(
            String userId, String permission) {
        return new PermissionIncorrectException("The user permission = " + permission
                + " for user with id = " + userId + " isn't correct", userId);
    }

    /**
     * Creates instance of PermissionIncorrectException if user try to change his own permissions for some entity.
     *
     * @param userId User's id
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithUserIdOnSelfPermissionChanges(String userId) {
        return new PermissionIncorrectException("The user permission for user with id = " + userId +
                " can't be changed by user himself", userId);
    }

    /**
     * Creates instance of PermissionIncorrectException if user permission can't be with unspecified user id.
     *
     * @return Instance of PermissionIncorrectException
     */
    public static PermissionIncorrectException createWithoutUserId() {
        return new PermissionIncorrectException("The user permission can't be with unspecified user id", null);
    }
}
