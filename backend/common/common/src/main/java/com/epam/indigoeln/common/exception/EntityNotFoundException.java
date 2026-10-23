package com.epam.indigoeln.common.exception;

import jakarta.ws.rs.core.Response;

public class EntityNotFoundException extends RuntimeException implements UserFacingException {

    public EntityNotFoundException(Object entityType, Object id) {
        super(String.format("%s %s not found", entityType, id));
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.NOT_FOUND;
    }
}
