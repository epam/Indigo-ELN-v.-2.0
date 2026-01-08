package com.epam.indigoeln.common.exception;

public class MutationNotUndoableException extends RuntimeException {

    public MutationNotUndoableException(String message) {
        super(message);
    }
}
