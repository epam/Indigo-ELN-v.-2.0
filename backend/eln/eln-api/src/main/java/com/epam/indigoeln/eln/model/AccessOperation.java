package com.epam.indigoeln.eln.model;

import org.jspecify.annotations.Nullable;

public enum AccessOperation {

    // all
    VIEW,
    EDIT,
    MANAGE_PERMISSIONS,
    MANAGE_ATTACHMENTS,
    // system
    CREATE_PROJECT,
    CREATE_TEMPLATE,
    EDIT_TEMPLATE,
    MANAGE_DICTIONARIES,
    // project
    LIST_NOTEBOOKS,
    CREATE_NOTEBOOK,
    // notebook
    LIST_EXPERIMENTS,
    CREATE_EXPERIMENT,
    // experiment
    MARK_EXPERIMENT,
    ;

    public boolean isAllowedBy(@Nullable AccessLevel accessLevel) {
        return accessLevel != null && accessLevel.allows(this);
    }
}
