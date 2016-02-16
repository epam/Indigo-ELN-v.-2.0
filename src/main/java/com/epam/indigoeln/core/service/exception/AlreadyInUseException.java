package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that links to Entity already exist.
 */
public class AlreadyInUseException extends CustomParametrizedException {

    public AlreadyInUseException(String message, String params) {
        super(message, params);
    }

    public static AlreadyInUseException createWithRoleId(String roleId) {
        return new AlreadyInUseException("The role with id = " + roleId + " already in use", roleId);
    }

    public static AlreadyInUseException createWithUserId(String userId) {
        return new AlreadyInUseException("The user with id = " + userId + " already in use", userId);
    }
}