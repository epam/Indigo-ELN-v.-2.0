package com.epam.indigoeln.core.service;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays, that Entity not found.
 */
public class EntityNotFoundException extends CustomParametrizedException {

    public EntityNotFoundException(String message, String params) {
        super(message, params);
    }

    public static EntityNotFoundException createWithProjectId(String projectId) {
        return new EntityNotFoundException("Project with id = " + projectId + " isn't found", projectId);
    }

    public static EntityNotFoundException createWithNotebookId(String notebookId) {
        return new EntityNotFoundException("Notebook with id = " + notebookId + " isn't found", notebookId);
    }

    public static EntityNotFoundException createWithExperimentId(String experimentId) {
        return new EntityNotFoundException("Experiment with id = " + experimentId + " isn't found", experimentId);
    }

    public static EntityNotFoundException createWithProjectChildId(String childId) {
        return new EntityNotFoundException("Project for child with id = " + childId + " isn't found", childId);
    }

    public static EntityNotFoundException createWithNotebookChildId(String childId) {
        return new EntityNotFoundException("Notebook for child with id = " + childId + " isn't found", childId);
    }

    public static EntityNotFoundException createWithProjectFileId(String fileId) {
        return new EntityNotFoundException("Project for file with id = " + fileId + " isn't found", fileId);
    }

    public static EntityNotFoundException createWithExperimentFileId(String fileId) {
        return new EntityNotFoundException("Experiment for file with id = " + fileId + " isn't found", fileId);
    }

    public static EntityNotFoundException createWithUserLogin(String login) {
        return new EntityNotFoundException("User with login = " + login + " isn't found", login);
    }

    public static EntityNotFoundException createWithUserId(String userId) {
        return new EntityNotFoundException("User with id = " + userId + " isn't found", userId);
    }

    public static EntityNotFoundException createWithRoleId(String roleId) {
        return new EntityNotFoundException("Role with id = " + roleId + " isn't found", roleId);
    }
}