package com.epam.indigoeln.common.exception;

public class MutationNotUndoableException extends RuntimeException implements UserFacingException {

    public MutationNotUndoableException(String message) {
        super(message);
    }
}
