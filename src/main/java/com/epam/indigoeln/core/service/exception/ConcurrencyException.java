package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception which displays that newer version of entity already exists.
 */
public class ConcurrencyException extends CustomParametrizedException {

    public ConcurrencyException(String message, Exception e, String params) {
        super(message, e, params);
    }

    public static ConcurrencyException createWithProjectName(String projectName, Exception e) {
        return new ConcurrencyException("Newer version of project " + projectName + " already exists. Please reopen it.", e, projectName);
    }

    public static ConcurrencyException createWithNotebookName(String notebookName, Exception e) {
        return new ConcurrencyException("Newer version of notebook " + notebookName + " already exists. Please reopen it.", e, notebookName);
    }

    public static ConcurrencyException createWithExperimentName(String experimentName, Exception e) {
        return new ConcurrencyException("Newer version of experiment " + experimentName + " already exists. Please reopen it.", e, experimentName);
    }

}