package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that links to Entity already exist.
 *
 * @author Timofey Zubkov
 */
public class AlreadyInUseException extends CustomParametrizedException {

    private static final long serialVersionUID = 5641713615602592557L;

    private AlreadyInUseException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of AlreadyInUseException class with role's id
     *
     * @param roleId Role's identifier
     * @return Instance of AlreadyInUseException class with role's id
     */
    public static AlreadyInUseException createWithRoleId(String roleId) {
        return new AlreadyInUseException("The role with id = " + roleId + " already in use", roleId);
    }

    /**
     * Creates instance of AlreadyInUseException class with user's id
     *
     * @param userId User's identifier
     * @return Instance of AlreadyInUseException class with user's id
     */
    public static AlreadyInUseException createWithUserId(String userId) {
        return new AlreadyInUseException("The user with id = " + userId + " already in use", userId);
    }

    /**
     * Creates instance of AlreadyInUseException class with template's id
     *
     * @param templateId Template's identifier
     * @return Instance of AlreadyInUseException class with template's id
     */
    public static AlreadyInUseException createWithTemplateId(String templateId) {
        return new AlreadyInUseException("The template with id = " + templateId + " already in use", String.valueOf(templateId));
    }

}