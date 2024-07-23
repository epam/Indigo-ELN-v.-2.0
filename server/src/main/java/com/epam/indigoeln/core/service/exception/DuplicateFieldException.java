/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception which displays that some field with such name is already exists.
 *
 * @author Evgeny Romanov
 */
public final class DuplicateFieldException extends CustomParametrizedException {

    private static final long serialVersionUID = -2792167348160969142L;

    private static final String EXCEPTION_MESSAGE = " is already in use";

    private DuplicateFieldException(String message, String params) {
        this(message, null, params);
    }

    private DuplicateFieldException(String message, Exception e, String params) {
        super(message, e, params);
    }

    private DuplicateFieldException(String message) {
        this(message, null);
    }

    /**
     * Creates instance of DuplicateFieldException class with notebook's
     * name if such name is already exists.
     *
     * @param name The name of notebook
     * @param e    Exception
     * @return Instance of DuplicateFieldException class with notebook's
     * name
     */
    public static DuplicateFieldException createWithNotebookName(String name, Exception e) {
        return new DuplicateFieldException("Notebook name " + name + EXCEPTION_MESSAGE, e, name);
    }

    /**
     * Creates instance of DuplicateFieldException class with project's
     * name if such name is already exists.
     *
     * @param name Project's name
     * @param e    Exception
     * @return Instance of DuplicateFieldException class with project's name
     */
    public static DuplicateFieldException createWithProjectName(String name, Exception e) {
        return new DuplicateFieldException("Project name " + name + EXCEPTION_MESSAGE, e, name);
    }

    /**
     * Creates instance of DuplicateFieldException class with template's
     * id if template with such primary key already exists.
     *
     * @param id Template's id
     * @param e  Exception
     * @return Instance of DuplicateFieldException class with template's name
     */
    public static DuplicateFieldException createWithTemplateId(String id, Exception e) {
        return new DuplicateFieldException("Template id =" + id + EXCEPTION_MESSAGE, e, id);
    }

    /**
     * Creates instance of DuplicateFieldException class with template's
     * name if such name is already exists.
     *
     * @return Instance of DuplicateFieldException class with template's name
     */
    public static DuplicateFieldException createWithTemplateName() {
        return new DuplicateFieldException("Template with this name already exists. Please unique name");
    }


    /**
     * Creates instance of DuplicateFieldException class with user's
     * login if such login is already exists.
     *
     * @param login Username
     * @return Instance of DuplicateFieldException class with user's
     * login
     */
    public static DuplicateFieldException createWithUserLogin(String login) {
        return new DuplicateFieldException("Login " + login + EXCEPTION_MESSAGE, login);
    }

    /**
     * Creates instance of DuplicateFieldException class with role if such role's name
     * is already exists.
     *
     * @param name Role's name
     * @param e    Exception
     * @return Instance of DuplicateFieldException class with role
     */
    public static DuplicateFieldException createWithRoleName(String name, Exception e) {
        return new DuplicateFieldException("Role name " + name + EXCEPTION_MESSAGE, e, name);
    }
}
