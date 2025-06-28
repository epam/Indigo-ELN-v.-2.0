package com.epam.indigoeln.eln.model;

import org.jspecify.annotations.Nullable;

public enum AccessOperation {

    // all
    VIEW,
    EDIT,
    MANAGE_PERMISSIONS,
    // system
    CREATE_PROJECT,
    CREATE_TEMPLATE,
    EDIT_TEMPLATE,
    MANAGE_DICTIONARIES,
    // project
    CREATE_NOTEBOOK,
    // notebook
    CREATE_EXPERIMENT,
    // experiment
    ;

    public boolean isAllowedBy(@Nullable AccessLevel accessLevel) {
        return accessLevel != null && accessLevel.allows(this);
    }
}
