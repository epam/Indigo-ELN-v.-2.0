package com.epam.indigoeln.eln.model;

import org.jspecify.annotations.Nullable;

public enum ApplicationPermission {

    // system
    MANAGE_USERS,
    MANAGE_ROLES,
    MANAGE_DICTIONARIES,
    MANAGE_TEMPLATES,

    // projects
    VIEW_PROJECTS,
    CREATE_PROJECTS,
    EDIT_PROJECTS,
    MANAGE_PROJECT_ACCESS,
    DELETE_PROJECTS,

    // notebooks
    VIEW_NOTEBOOKS,
    CREATE_NOTEBOOKS,
    EDIT_NOTEBOOKS,
    MANAGE_NOTEBOOK_ACCESS,
    DELETE_NOTEBOOKS,

    // experiments
    VIEW_EXPERIMENTS,
    CREATE_EXPERIMENTS,
    EDIT_EXPERIMENTS,
    MANAGE_EXPERIMENT_ACCESS,
    DELETE_EXPERIMENTS,
    SUBMIT_EXPERIMENTS,

    // signatures
    SIGN_EXPERIMENTS,
    ;

    public boolean isAllowedBy(@Nullable AccessLevel accessLevel) {
        return accessLevel != null && accessLevel.allows(this);
    }
}
