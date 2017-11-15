package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;


/**
 * Custom exception, which displays that parent Entity has child reference.
 *
 * @author Timofey Zubkov
 */
public class ChildReferenceException extends CustomParametrizedException {

    /**
     * Creates instance of ChildReferenceException with parent's id
     *
     * @param parentId parent's identifier
     */
    public ChildReferenceException(String parentId) {
        super("The parent with id = " + parentId + " has child reference", parentId);
    }
}