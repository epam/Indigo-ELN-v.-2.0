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
        return new EntityNotFoundException("Project with id = " + projectId + " not found", projectId);
    }

    public static EntityNotFoundException createWithNotebookId(String notebookId) {
        return new EntityNotFoundException("Notebook with id = " + notebookId + " not found", notebookId);
    }

    public static EntityNotFoundException createWithExperimentId(String experimentId) {
        return new EntityNotFoundException("Experiment with id = " + experimentId + " not found", experimentId);
    }

    public static EntityNotFoundException createWithProjectChildId(String childId) {
        return new EntityNotFoundException("Project for child with id = " + childId + " not found", childId);
    }

    public static EntityNotFoundException createWithNotebookChildId(String childId) {
        return new EntityNotFoundException("Notebook for child with id = " + childId + " not found", childId);
    }
}