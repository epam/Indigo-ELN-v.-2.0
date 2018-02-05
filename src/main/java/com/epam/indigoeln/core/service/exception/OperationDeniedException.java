package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays that operation was denied.
 *
 * @author Timofey Zubkov
 */
public final class OperationDeniedException extends CustomParametrizedException {

    private static final String PROJECT_ENTITY_NAME = "project";
    private static final String NOTEBOOK_ENTITY_NAME = "notebook";

    /**
     * Creates instance of OperationDeniedException.
     *
     * @param message Exception message
     * @param params  Exception parameters
     */
    private OperationDeniedException(String message, String... params) {
        super(message, params);
    }

    /**
     * Creates instance of OperationDeniedException with user's id if current user tried to delete himself.
     *
     * @param userId User's identifier
     * @return Instance of OperationDeniedException with user's id
     */
    public static OperationDeniedException createUserDeleteOperation(String userId) {
        return new OperationDeniedException("The current user can't delete himself", userId);
    }

    public static OperationDeniedException createUserWithNotValidPassword() {
        return new OperationDeniedException("User password should have min length 6, and contains"
                + "at least one capital and at least one digit");
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for reading.
     *
     * @param entityId Entity's id
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createProjectSubEntitiesReadOperation(String entityId) {
        return createSubEntitiesReadOperation(entityId, PROJECT_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for reading.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createNotebookSubEntitiesReadOperation(String entityId) {
        return createSubEntitiesReadOperation(entityId, NOTEBOOK_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for reading.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createProjectReadOperation(String entityId) {
        return createEntityReadOperation(entityId, PROJECT_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for reading.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createNotebookReadOperation(String entityId) {
        return createEntityReadOperation(entityId, NOTEBOOK_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for reading.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createExperimentReadOperation(String entityId) {
        return createEntityReadOperation(entityId, "experiment");
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if current user doesn't have permissions for creating.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createProjectSubEntityCreateOperation(String entityId) {
        return createSubEntityCreateOperation(entityId, PROJECT_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to update experiment which's status isn't 'OPEN'.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createNotOpenExperimentUpdateOperation(String entityId) {
        return new OperationDeniedException("The experiment doesn't have status Open and can't be updated", entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to reopen experiment.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createExperimentReopenOperation(String entityId) {
        return new OperationDeniedException("The open experiment can't be reopened", entityId);
    }


    /**
     * Creates instance of OperationDeniedException with entity's id and message
     * if user tried to reopen experiment.
     *
     * @param entityId Entity's identifier
     * @param message  Message
     * @return Instance of OperationDeniedException with entity's id and message
     */
    public static OperationDeniedException createExperimentReopenOperation(String entityId, String message) {
        return new OperationDeniedException(message, entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to create notebook.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createNotebookSubEntityCreateOperation(String entityId) {
        return createSubEntityCreateOperation(entityId, NOTEBOOK_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to update project.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createProjectUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, PROJECT_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to update notebook.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createNotebookUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, NOTEBOOK_ENTITY_NAME);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to update experiment.
     *
     * @param entityId Entity's identifier
     * @return Instance of OperationDeniedException with entity's id
     */
    public static OperationDeniedException createExperimentUpdateOperation(String entityId) {
        return createEntityUpdateOperation(entityId, "experiment");
    }

    /**
     * Creates instance of OperationDeniedException with entity's id and name
     * if user tried to read sub entities.
     *
     * @param entityId   Entity's identifier
     * @param entityName Entity's name
     * @return Instance of OperationDeniedException with entity's id and name
     */
    private static OperationDeniedException createSubEntitiesReadOperation(String entityId, String entityName) {
        return new OperationDeniedException(
                String.format("The current user doesn't have permissions to read sub-entities of %s with id = %s",
                        entityName, entityId), entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id and name
     * if user tried to read entity.
     *
     * @param entityId   Entity's identifier
     * @param entityName Entity's name
     * @return Instance of OperationDeniedException with entity's id and name
     */
    private static OperationDeniedException createEntityReadOperation(String entityId, String entityName) {
        return new OperationDeniedException(String.format("The current user doesn't have permissions"
                + " to read %s with id = %s", entityName, entityId), entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id and name
     * if user tried to create sub entity.
     *
     * @param entityId   Entity's identifier
     * @param entityName Entity's name
     * @return Instance of OperationDeniedException with entity's id and name
     */
    private static OperationDeniedException createSubEntityCreateOperation(String entityId, String entityName) {
        return new OperationDeniedException(String.format("The current user doesn't have permissions"
                + " to create sub-entity of %s with id = %s", entityName, entityId), entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id and name
     * if user tried to update.
     *
     * @param entityId   Entity's identifier
     * @param entityName Entity's name
     * @return Instance of OperationDeniedException with entity's id and name
     */
    private static OperationDeniedException createEntityUpdateOperation(String entityId, String entityName) {
        return new OperationDeniedException(String.format("The current user doesn't have permissions "
                + "to update %s with id = %s", entityName, entityId), entityId);
    }

    /**
     * Creates instance of OperationDeniedException with entity's id
     * if user tried to create version of experiment.
     *
     * @param entityId Entity's identifier
     * @return Entity's name
     */
    public static OperationDeniedException createVersionExperimentOperation(String entityId) {
        return new OperationDeniedException("A new version of experiment can't be created"
                + " while there is open experiment", entityId);
    }

    /**
     * Creates instance of OperationDeniedException
     * if user tried to update notebook if there is at least one batch or open experiment.
     *
     * @return Instance of OperationDeniedException
     */
    public static OperationDeniedException createNotebookUpdateNameOperation() {
        return new OperationDeniedException("The notebook can't be updated if there is at least one experiment "
                + "with a batch or experiments in non-Open status");
    }
}
