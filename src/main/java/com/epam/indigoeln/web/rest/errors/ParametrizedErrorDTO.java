package com.epam.indigoeln.web.rest.errors;

import java.io.Serializable;
import java.util.Arrays;

/**
 * DTO for sending a parametrized error message.
 */
public class ParametrizedErrorDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String message;
    private final String[] params;

    public ParametrizedErrorDTO(String message, String... params) {
        this.message = message;
        this.params = params;
    }

    public String getMessage() {
        return message;
    }

    public String[] getParams() {
        return Arrays.copyOf(params, params.length);
    }

}
