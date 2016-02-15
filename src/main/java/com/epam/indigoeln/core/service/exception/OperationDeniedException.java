package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class OperationDeniedException extends CustomParametrizedException {

    public OperationDeniedException(String message, String params) {
        super(message, params);
    }

    public static OperationDeniedException createUserDeleteOperation(String userId) {
        return new OperationDeniedException("The current user can't delete himself", userId);
    }

    public static OperationDeniedException createUserRolesUpdateOperation(String userId) {
        return new OperationDeniedException("The current user can't change roles to himself", userId);
    }

    public static OperationDeniedException createProjectSubEntitiesReadOperation(String entityId) {
        return createSubEntitiesReadOperation(entityId, "project");
    }

    public static OperationDeniedException createNotebookSubEntitiesReadOperation(String entityId) {
        return createSubEntitiesReadOperation(entityId, "notebook");
    }

    public static OperationDeniedException createProjectReadOperation(String entityId) {
        return createEntityReadOperation(entityId, "project");
    }

    public static OperationDeniedException createNotebookReadOperation(String entityId) {
        return createEntityReadOperation(entityId, "notebook");
    }

    public static OperationDeniedException createExperimentReadOperation(String entityId) {
        return createEntityReadOperation(entityId, "experiment");
    }

    public static OperationDeniedException createProjectSubEntityCreateOperation(String entityId) {
        return createSubEntityCreateOperation(entityId, "project");
    }

    public static OperationDeniedException createNotebookSubEntityCreateOperation(String entityId) {
        return createSubEntityCreateOperation(entityId, "notebook");
    }

    public static OperationDeniedException createProjectUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, "project");
    }

    public static OperationDeniedException createNotebookUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, "notebook");
    }

    public static OperationDeniedException createExperimentUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, "experiment");
    }

    private static OperationDeniedException createSubEntitiesReadOperation(String entityId, String entityName) {
        return new OperationDeniedException("The current user doesn't have permissions to read sub-entities of " +
                entityName + " with id = " + entityId, entityId);
    }

    private static OperationDeniedException createEntityReadOperation(String entityId, String entityName) {
        return new OperationDeniedException("The current user doesn't have permissions to read " +
                entityName + " with id = " + entityId, entityId);
    }

    private static OperationDeniedException createSubEntityCreateOperation(String entityId, String entityName) {
        return new OperationDeniedException("The current user doesn't have permissions to create sub-entity of " +
                entityName + " with id = " + entityId, entityId);
    }

    private static OperationDeniedException createEntityUpdateOperation(String entityId, String entityName) {
        return new OperationDeniedException("The current user doesn't have permissions to update " +
                entityName + " with id = " + entityId, entityId);
    }
}