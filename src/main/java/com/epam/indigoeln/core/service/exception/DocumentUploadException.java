package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class DocumentUploadException extends CustomParametrizedException {

    public DocumentUploadException(String experimentId) {
        super("Document for the experiment " + experimentId + " failed uploading to signature service.");
    }
}
