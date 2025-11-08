package com.epam.indigoeln.common.exception;

import java.util.UUID;

public class IncorrectRevisionException extends RuntimeException {

    public IncorrectRevisionException(Enum<?> entityType, UUID entityID, Integer requestedRevision, Integer currentRevision) {
        super(String.format("%s %s: incorrect revision %d requested; current revision %d", entityType, entityID, requestedRevision, currentRevision));
    }
}
