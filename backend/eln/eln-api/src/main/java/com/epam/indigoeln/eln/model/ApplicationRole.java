package com.epam.indigoeln.eln.model;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@RequiredArgsConstructor
public enum ApplicationRole {

    ADMINISTRATOR(EnumSet.of(
            AccessOperation.MANAGE_DICTIONARIES
    )),
    CONTENT_EDITOR(EnumSet.of(
            AccessOperation.VIEW,
            AccessOperation.EDIT,
            AccessOperation.CREATE_PROJECT,
            AccessOperation.CREATE_NOTEBOOK,
            AccessOperation.CREATE_EXPERIMENT
    )),
    TEMPLATE_EDITOR(EnumSet.of(
            AccessOperation.CREATE_TEMPLATE,
            AccessOperation.EDIT_TEMPLATE
    ));

    private final Set<AccessOperation> grants;

    public boolean allows(AccessOperation operation) {
        return grants.contains(operation);
    }
}
