package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.IncorrectAuthoritiesException;

import java.util.Set;

public final class AuthoritiesUtil {
    private AuthoritiesUtil() {

    }

    public static void checkAuthorities(Set<Authority> authorities) {
        if (authorities.contains(Authority.PROJECT_CREATOR) && !authorities.contains(Authority.PROJECT_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.PROJECT_READER);
        } else if (authorities.contains(Authority.NOTEBOOK_CREATOR)
                && !authorities.contains(Authority.NOTEBOOK_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.NOTEBOOK_READER);
        } else if (authorities.contains(Authority.EXPERIMENT_CREATOR)
                && !authorities.contains(Authority.EXPERIMENT_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.EXPERIMENT_READER);
        }
    }
}
