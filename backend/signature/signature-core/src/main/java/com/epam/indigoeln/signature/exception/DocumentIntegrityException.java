package com.epam.indigoeln.signature.exception;

import com.epam.indigoeln.common.exception.UserFacingException;
import jakarta.ws.rs.core.Response;

public class DocumentIntegrityException extends RuntimeException implements UserFacingException {

    public DocumentIntegrityException(String message) {
        super(message);
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.CONFLICT;
    }
}
