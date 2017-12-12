package com.epam.indigoeln.core.service.exception;

import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

public class IncorrectAuthoritiesException extends CustomParametrizedException {

    private IncorrectAuthoritiesException(String message, String... params) {
        super(message, params);
    }

    public static IncorrectAuthoritiesException create(Authority authority) {
        return new IncorrectAuthoritiesException(String.format("Should be chosen %s",
                authority.getAuthority()));
    }
}
