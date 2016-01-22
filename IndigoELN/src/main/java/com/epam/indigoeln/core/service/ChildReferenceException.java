package com.epam.indigoeln.core.service;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;


/**
 * Custom exception, which displays that parent Entity has child reference.
 */
public class ChildReferenceException extends CustomParametrizedException {

    public ChildReferenceException(String parentId) {
        super("Parent with id = " + parentId + " has child reference", parentId);
    }
}