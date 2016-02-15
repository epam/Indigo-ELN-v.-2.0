package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that Entity not found.
 */
public class EntityNotFoundException extends CustomParametrizedException {

    public EntityNotFoundException(String message, String params) {
        super(message, params);
    }

    public static EntityNotFoundException createWithProjectId(String projectId) {
        return new EntityNotFoundException("The project with id = " + projectId + " isn't found", projectId);
    }

    public static EntityNotFoundException createWithNotebookId(String notebookId) {
        return new EntityNotFoundException("The notebook with id = " + notebookId + " isn't found", notebookId);
    }

    public static EntityNotFoundException createWithExperimentId(String experimentId) {
        return new EntityNotFoundException("The experiment with id = " + experimentId + " isn't found", experimentId);
    }

    public static EntityNotFoundException createWithProjectChildId(String childId) {
        return new EntityNotFoundException("The project for child with id = " + childId + " isn't found", childId);
    }

    public static EntityNotFoundException createWithNotebookChildId(String childId) {
        return new EntityNotFoundException("The notebook for child with id = " + childId + " isn't found", childId);
    }

    public static EntityNotFoundException createWithProjectFileId(String fileId) {
        return new EntityNotFoundException("The project for file with id = " + fileId + " isn't found", fileId);
    }

    public static EntityNotFoundException createWithExperimentFileId(String fileId) {
        return new EntityNotFoundException("The experiment for file with id = " + fileId + " isn't found", fileId);
    }

    public static EntityNotFoundException createWithUserLogin(String login) {
        return new EntityNotFoundException("The user with login = " + login + " isn't found", login);
    }

    public static EntityNotFoundException createWithUserId(String userId) {
        return new EntityNotFoundException("The user with id = " + userId + " isn't found", userId);
    }

    public static EntityNotFoundException createWithRoleId(String roleId) {
        return new EntityNotFoundException("The role with id = " + roleId + " isn't found", roleId);
    }
}