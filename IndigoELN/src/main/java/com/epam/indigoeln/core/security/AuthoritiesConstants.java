package com.epam.indigoeln.core.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
    public static final String VIEWER = "ROLE_VIEWER";
    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String PROJECT_READER = "ROLE_PROJECT_READER";
    public static final String PROJECT_CREATOR = "ROLE_PROJECT_CREATOR";
    public static final String PROJECT_REMOVER = "ROLE_PROJECT_REMOVER";

    public static final String NOTEBOOK_READER = "ROLE_NOTEBOOK_READER";
    public static final String NOTEBOOK_CREATOR = "ROLE_NOTEBOOK_CREATOR";
    public static final String NOTEBOOK_REMOVER = "ROLE_NOTEBOOK_REMOVER";

    public static final String EXPERIMENT_READER = "ROLE_EXPERIMENT_READER";
    public static final String EXPERIMENT_CREATOR = "ROLE_EXPERIMENT_CREATOR";
    public static final String EXPERIMENT_REMOVER = "ROLE_EXPERIMENT_REMOVER";

    private AuthoritiesConstants() {
    }
}