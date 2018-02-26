/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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
