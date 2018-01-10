package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that Entity not found.
 *
 * @author Timofey Zubkov
 */
public final class EntityNotFoundException extends CustomParametrizedException {

    private static final String EXCEPTION_MESSAGE = " isn't found";

    /**
     * Creates instance of EntityNotFoundException.
     *
     * @param message Exception message
     * @param params  Exception parameters
     */
    private EntityNotFoundException(String message, String params) {
        super(message, params);
    }

    /**
     * Creates instance of EntityNotFoundException class with project's id
     * if such id is not found.
     *
     * @param projectId Project's identifier
     * @return Instance of EntityNotFoundException class with project's id
     */
    public static EntityNotFoundException createWithProjectId(String projectId) {
        return new EntityNotFoundException("The project with id = " + projectId + EXCEPTION_MESSAGE, projectId);
    }

    /**
     * Creates instance of EntityNotFoundException class with notebook's id
     * if such id is not found.
     *
     * @param notebookId Notebook's identifier
     * @return Instance of EntityNotFoundException class with notebook's id
     */
    public static EntityNotFoundException createWithNotebookId(String notebookId) {
        return new EntityNotFoundException("The notebook with id = " + notebookId + EXCEPTION_MESSAGE, notebookId);
    }

    /**
     * Creates instance of EntityNotFoundException class with experiment's id
     * if such id is not found.
     *
     * @param experimentId Experiment's identifier
     * @return Instance of EntityNotFoundException class with experiment's id
     */
    public static EntityNotFoundException createWithExperimentId(String experimentId) {
        return new EntityNotFoundException("The experiment with id = " + experimentId
                + EXCEPTION_MESSAGE, experimentId);
    }

    /**
     * Creates instance of EntityNotFoundException class with child's id
     * if such id is not found.
     *
     * @param childId Child's identifier
     * @return Instance of EntityNotFoundException class with child's id
     */
    public static EntityNotFoundException createWithProjectChildId(String childId) {
        return new EntityNotFoundException("The project for child with id = " + childId + EXCEPTION_MESSAGE, childId);
    }

    /**
     * Creates instance of EntityNotFoundException class with child's id
     * if such id is not found.
     *
     * @param childId Child's identifier
     * @return Instance of EntityNotFoundException class with child's id
     */
    public static EntityNotFoundException createWithNotebookChildId(String childId) {
        return new EntityNotFoundException("The notebook for child with id = " + childId + EXCEPTION_MESSAGE, childId);
    }

    /**
     * Creates instance of EntityNotFoundException class with experiment's name
     * if such name is not found.
     *
     * @param experimentName Experiment's name
     * @return Instance of EntityNotFoundException class with experiment's name
     */
    public static EntityNotFoundException createWithExperimentName(String experimentName) {
        return new EntityNotFoundException("The latest version of experiment with name = "
                + experimentName + EXCEPTION_MESSAGE, experimentName);
    }

    /**
     * Creates instance of EntityNotFoundException class with file's id
     * if such id is not found.
     *
     * @param fileId File's identifier
     * @return Instance of EntityNotFoundException class with file's id
     */
    public static EntityNotFoundException createWithExperimentFileId(String fileId) {
        return new EntityNotFoundException("The experiment for file with id = " + fileId + EXCEPTION_MESSAGE, fileId);
    }

    /**
     * Creates instance of EntityNotFoundException class with login
     * if such login is not found.
     *
     * @param login Username
     * @return Instance of EntityNotFoundException class with login
     */
    public static EntityNotFoundException createWithUserLogin(String login) {
        return new EntityNotFoundException("The user with login = " + login + EXCEPTION_MESSAGE, login);
    }

    /**
     * Creates instance of EntityNotFoundException class with user's id
     * if such id is not found.
     *
     * @param userId User's identifier
     * @return Instance of EntityNotFoundException class with user's id
     */
    public static EntityNotFoundException createWithUserId(String userId) {
        return new EntityNotFoundException("The user with id = " + userId + EXCEPTION_MESSAGE, userId);
    }

    /**
     * Creates instance of EntityNotFoundException class with role's id
     * if such id is not found.
     *
     * @param roleId Role's identifier
     * @return Instance of EntityNotFoundException class with role's id
     */
    public static EntityNotFoundException createWithRoleId(String roleId) {
        return new EntityNotFoundException("The role with id = " + roleId + EXCEPTION_MESSAGE, roleId);
    }

    /**
     * Creates instance of EntityNotFoundException class with template's id
     * if such id is not found.
     *
     * @param templateId Template's identifier
     * @return Instance of EntityNotFoundException class with template's id
     */
    public static EntityNotFoundException createWithTemplateId(String templateId) {
        return new EntityNotFoundException("The template with id = " + templateId + EXCEPTION_MESSAGE, templateId);
    }

    /**
     * Creates instance of EntityNotFoundException class with dictionary's id
     * if such id is not found.
     *
     * @param dictionaryId Dictionary's identifier
     * @return Instance of EntityNotFoundException class with dictionary's id
     */
    public static EntityNotFoundException createWithDictionaryId(String dictionaryId) {
        return new EntityNotFoundException("The dictionary with id = " + dictionaryId
                + EXCEPTION_MESSAGE, dictionaryId);
    }
}
