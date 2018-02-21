package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

/**
 * Custom exception, which displays that operation not available.
 */
public class OperationNotAvailableException extends CustomParametrizedException {
    private OperationNotAvailableException(String message, String... params) {
        super(message, params);
    }

    /**
     * Creates instance of OperationNotAvailableException.
     * if user tried to update notebook if there is at least one batch or open experiment.
     *
     * @return Instance of OperationDeniedException
     */
    public static OperationNotAvailableException createNotebookUpdateNameOperation() {
        return new OperationNotAvailableException("The notebook can't be updated if there is at least one experiment "
                + "with a batch or experiments in non-Open status");
    }
}
