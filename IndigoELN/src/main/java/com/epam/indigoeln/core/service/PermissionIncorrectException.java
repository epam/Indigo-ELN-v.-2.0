package com.epam.indigoeln.core.service;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that Entity not found.
 */
public class PermissionIncorrectException extends CustomParametrizedException {

    public PermissionIncorrectException(String message, String params) {
        super(message, params);
    }

    public static PermissionIncorrectException createWithUserIdAndPermission(
            String userId, String permission) {
        return new PermissionIncorrectException("The user permission = " + permission +
                " for user with id = " + userId + " isn't correct", userId);
    }

    public static PermissionIncorrectException createWithoutUserId() {
        return new PermissionIncorrectException("The user permission can't be with unspecified user id", null);
    }
}