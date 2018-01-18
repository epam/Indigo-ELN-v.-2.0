package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that links to Entity already exist.
 *
 * @author Timofey Zubkov
 */
public final class AlreadyInUseException extends CustomParametrizedException {

    private static final long serialVersionUID = 5641713615602592557L;

    private AlreadyInUseException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of AlreadyInUseException class with role's id.
     *
     * @param roleId Role's identifier
     * @return Instance of AlreadyInUseException class with role's id
     */
    public static AlreadyInUseException createWithRoleId(String roleId) {
        return new AlreadyInUseException("The role with id = " + roleId + " already in use", roleId);
    }
}
