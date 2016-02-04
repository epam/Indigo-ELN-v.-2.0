package com.epam.indigoeln.core.service;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that Entity already exists.
 */
public class EntityAlreadyExistsException extends CustomParametrizedException {

    public EntityAlreadyExistsException(String message, String params) {
        super(message, params);
    }

    public static EntityAlreadyExistsException createWithUserLogin(String login) {
        return new EntityAlreadyExistsException("The user with login = " + login + " already exists", login);
    }

    public static EntityAlreadyExistsException createWithRoleName(String name) {
        return new EntityAlreadyExistsException("The role with name = " + name + " already exists", name);
    }

    public static EntityAlreadyExistsException createWithNotebookName(String name) {
        return new EntityAlreadyExistsException("The notebook with name = " + name + " already exists", name);
    }
}