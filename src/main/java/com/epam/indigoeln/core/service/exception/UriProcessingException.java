package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class UriProcessingException extends CustomParametrizedException {

    private UriProcessingException(String message, String... params) {
        super(message, params);
    }

    /**
     * Creates instance of UriProcessingException class with field name that can't be processed.
     *
     * @param fieldName Field name that can't be processed
     * @return Instance of UriProcessingException class with field name that can't be processed
     */
    public static UriProcessingException cantParseSortingField(String fieldName) {
        return new UriProcessingException(String.format("Cant parse sorting param field '%s'", fieldName), fieldName);
    }
}
