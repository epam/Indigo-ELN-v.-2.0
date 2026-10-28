package com.epam.indigoeln.common.exception;

import jakarta.ws.rs.core.Response;

/**
 * Marks an exception whose message is written for the end user and is returned to the client as is.
 * Any other exception is treated as internal: the client gets a generic message with an error id.
 */
public interface UserFacingException {

    String getMessage();

    default Response.Status getStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
