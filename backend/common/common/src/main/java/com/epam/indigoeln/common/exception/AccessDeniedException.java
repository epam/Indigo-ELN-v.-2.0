package com.epam.indigoeln.common.exception;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(Enum<?> entityType, UUID id, String currentUser) {
        super(String.format("%s %s not found or not accessible; current user %s", entityType, id, currentUser));
    }

    public AccessDeniedException(Enum<?> operation, String currentUser) {
        super(String.format("Operation not permitted: %s; current user %s", operation, currentUser));
    }

    public AccessDeniedException(Enum<?> entityType, UUID id, Enum<?> operation, @Nullable Enum<?> currentLevel, String currentUser) {
        super(String.format("Operation not permitted: %s on %s %s; current access %s doesn't allow it; current user %s", operation, entityType, id, currentLevel, currentUser));
    }

    public AccessDeniedException(String missingUser) {
        super(String.format("User not found: %s", missingUser));
    }
}
