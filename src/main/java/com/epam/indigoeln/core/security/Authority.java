package com.epam.indigoeln.core.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enum for Spring Security authorities.
 */
public enum Authority implements GrantedAuthority {
    ANONYMOUS,

    USER_EDITOR,
    ROLE_EDITOR,
    CONTENT_EDITOR,
    TEMPLATE_EDITOR,
    DICTIONARY_EDITOR,

    PROJECT_READER,
    PROJECT_CREATOR,
    PROJECT_REMOVER,

    NOTEBOOK_READER,
    NOTEBOOK_CREATOR,
    NOTEBOOK_REMOVER,

    EXPERIMENT_READER,
    EXPERIMENT_CREATOR,
    EXPERIMENT_REMOVER;

    @Override
    public String getAuthority() {
        return name();
    }

}