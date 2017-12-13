package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception which displays that newer version of entity already exists.
 *
 * @author Evgeny Romanov
 */
public final class ConcurrencyException extends CustomParametrizedException {

    private static final String EXCEPTION_MESSAGE = " already exists. Please reopen it.";

    private ConcurrencyException(String message, Exception e, String params) {
        super(message, e, params);
    }

    /**
     * Creates instance of ConcurrencyException with project's name.
     *
     * @param projectName Project's name
     * @param e           Exception
     * @return Instance of ConcurrencyException with project name
     */
    public static ConcurrencyException createWithProjectName(String projectName, Exception e) {
        return new ConcurrencyException("Newer version of project " + projectName + EXCEPTION_MESSAGE, e, projectName);
    }

    /**
     * Creates instance of ConcurrencyException with template's name.
     *
     * @param templateName Template's name
     * @param e            Exception
     * @return Instance of ConcurrencyException with template's name
     */
    public static ConcurrencyException createWithTemplateName(String templateName, Exception e) {
        return new ConcurrencyException("Newer version of template " + templateName
                + EXCEPTION_MESSAGE, e, templateName);
    }

    /**
     * Creates instance of ConcurrencyException with notebook's name.
     *
     * @param notebookName Notebook's name
     * @param e            Exception
     * @return Instance of ConcurrencyException with notebook's name
     */
    public static ConcurrencyException createWithNotebookName(String notebookName, Exception e) {
        return new ConcurrencyException("Newer version of notebook " + notebookName
                + EXCEPTION_MESSAGE, e, notebookName);
    }

    /**
     * Creates instance of ConcurrencyException with experiment's name.
     *
     * @param experimentName Experiment's name
     * @param e              Exception
     * @return Instance of ConcurrencyException with experiment's name
     */
    public static ConcurrencyException createWithExperimentName(String experimentName, Exception e) {
        return new ConcurrencyException("Newer version of experiment " + experimentName
                + EXCEPTION_MESSAGE, e, experimentName);
    }
}