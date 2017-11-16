package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;


/**
 * Custom exception, which displays that File not found.
 *
 * @author Timofey Zubkov
 */
public class FileNotFoundException extends CustomParametrizedException {

    public FileNotFoundException(String fileId) {
        super("The file with id = " + fileId + " not found", fileId);
    }
}