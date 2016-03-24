package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class DocumentUploadException extends CustomParametrizedException {

    public DocumentUploadException(String message, String params) {
        super(message, params);
    }

    public static DocumentUploadException createFailedUploading(String experimentId) {
        return new DocumentUploadException("Document for the experiment " + experimentId + " failed uploading to signature service.", experimentId);
    }

    public static DocumentUploadException createNullDocumentId(String experimentId) {
        return new DocumentUploadException("Document for the experiment " + experimentId + " is missed.", experimentId);
    }
}
