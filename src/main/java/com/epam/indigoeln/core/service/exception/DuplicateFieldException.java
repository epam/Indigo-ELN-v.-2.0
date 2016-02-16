package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class DuplicateFieldException extends CustomParametrizedException {

    public DuplicateFieldException(String message, String params) {
        super(message, params);
    }

    public static DuplicateFieldException createWithNotebookName(String name) {
        return new DuplicateFieldException("Notebook name " + name + " is already in use", name);
    }

    public static DuplicateFieldException createWithUserLogin(String login) {
        return new DuplicateFieldException("Login " + login + " is already in use", login);
    }

    public static DuplicateFieldException createWithRoleName(String name) {
        return new DuplicateFieldException("Role name " + name + " is already in use", name);
    }

}
