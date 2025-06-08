package com.epam.indigoeln.common.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Enum<?> entityType, UUID id) {
        super(String.format("%s %s not found", entityType, id));
    }
}
