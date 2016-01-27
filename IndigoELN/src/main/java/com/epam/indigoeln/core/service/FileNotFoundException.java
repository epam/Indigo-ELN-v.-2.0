package com.epam.indigoeln.core.service;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;


/**
 * Custom exception, which displays that File not found.
 */
public class FileNotFoundException extends CustomParametrizedException {

    public FileNotFoundException(String fileId) {
        super("File with id = " + fileId + " not found", fileId);
    }
}