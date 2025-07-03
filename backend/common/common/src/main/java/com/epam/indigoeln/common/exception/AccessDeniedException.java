package com.epam.indigoeln.common.exception;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(Enum<?> entityType, UUID id, String currentUser, Object[] currentRoles) {
        super(String.format("%s %s not found or not accessible; current user %s with roles %s", entityType, id, currentUser, Arrays.toString(currentRoles)));
    }

    public AccessDeniedException(Enum<?> operation, String currentUser, Object[] currentRoles) {
        super(String.format("Operation not permitted: %s; current user %s with roles %s", operation, currentUser, Arrays.toString(currentRoles)));
    }

    public AccessDeniedException(Enum<?> entityType, UUID id, Enum<?> operation, @Nullable Enum<?> currentLevel, String currentUser, Object[] currentRoles) {
        super(String.format("Operation not permitted: %s on %s %s; current access %s doesn't allow it; current user %s with roles %s", operation, entityType, id, currentLevel, currentUser, Arrays.toString(currentRoles)));
    }

    public AccessDeniedException(String missingUser) {
        super(String.format("User not found: %s", missingUser));
    }
}
