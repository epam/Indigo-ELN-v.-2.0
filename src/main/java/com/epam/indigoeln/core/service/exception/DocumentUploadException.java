package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception which displays that uploading is failed.
 *
 * @author Anton Pikhtin
 */
public final class DocumentUploadException extends CustomParametrizedException {

    private DocumentUploadException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of DocumentUploadException class with experiment's id
     * if uploading is failed.
     *
     * @param experimentId Experiment's identifier
     * @return Instance of DocumentUploadException class with experiment's id
     */
    public static DocumentUploadException createFailedUploading(String experimentId) {
        return new DocumentUploadException("Document for the experiment " + experimentId
                + " failed uploading to signature service.", experimentId);
    }

    /**
     * Creates instance of DocumentUploadException class with experiment's id
     * if document for the experiment is missed.
     *
     * @param experimentId Experiment's identifier
     * @return Instance of DocumentUploadException class with experiment's id
     */
    public static DocumentUploadException createNullDocumentId(String experimentId) {
        return new DocumentUploadException("Document for the experiment " + experimentId + " is missed.", experimentId);
    }
}