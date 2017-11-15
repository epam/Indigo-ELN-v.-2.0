package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception which displays that newer version of entity already exists.
 */
public class ConcurrencyException extends CustomParametrizedException {

    private static final String EXCEPTION_MESSAGE = " already exists. Please reopen it.";

    private ConcurrencyException(String message, Exception e, String params) {
        super(message, e, params);
    }

    /**
     * Creates instance of ConcurrencyException with project name
     * @param projectName
     * @param e
     * @return
     */
    public static ConcurrencyException createWithProjectName(String projectName, Exception e) {
        return new ConcurrencyException("Newer version of project " + projectName + EXCEPTION_MESSAGE, e, projectName);
    }

    public static ConcurrencyException createWithTemplateName(String templateName, Exception e) {
        return new ConcurrencyException("Newer version of template " + templateName + EXCEPTION_MESSAGE, e, templateName);
    }

    public static ConcurrencyException createWithNotebookName(String notebookName, Exception e) {
        return new ConcurrencyException("Newer version of notebook " + notebookName + EXCEPTION_MESSAGE, e, notebookName);
    }

    public static ConcurrencyException createWithExperimentName(String experimentName, Exception e) {
        return new ConcurrencyException("Newer version of experiment " + experimentName + EXCEPTION_MESSAGE, e, experimentName);
    }

}