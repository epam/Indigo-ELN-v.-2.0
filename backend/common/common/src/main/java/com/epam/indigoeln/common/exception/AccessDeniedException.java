package com.epam.indigoeln.common.exception;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(Object entityType, UUID id) {
        super(String.format("%s %s not found or not accessible", entityType, id));
    }

    public AccessDeniedException(Enum<?> operation) {
        super(String.format("Operation not permitted: %s", operation));
    }

    public AccessDeniedException(Object entityType, UUID id, Enum<?> operation, @Nullable Enum<?> currentLevel) {
        super(String.format("Operation not permitted: %s on %s %s; current access %s doesn't allow it", operation, entityType, id, currentLevel));
    }

    public AccessDeniedException(String missingUser) {
        super(String.format("User not found: %s", missingUser));
    }
}
