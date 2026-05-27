package com.epam.indigoeln.common.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Object entityType, Object id) {
        super(String.format("%s %s not found", entityType, id));
    }
}
