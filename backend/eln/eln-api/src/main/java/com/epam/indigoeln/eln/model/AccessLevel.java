package com.epam.indigoeln.eln.model;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@RequiredArgsConstructor
public enum AccessLevel {
    NONE(EnumSet.noneOf(AccessOperation.class)),
    IMPLICIT_VIEW(EnumSet.of(
            AccessOperation.VIEW
    )),
    VIEW(EnumSet.of(
            AccessOperation.VIEW
    )),
    EDIT(EnumSet.of(
            AccessOperation.VIEW,
            AccessOperation.EDIT,
            AccessOperation.CREATE_NOTEBOOK,
            AccessOperation.CREATE_EXPERIMENT
    )),
    ADMIN(EnumSet.allOf(AccessOperation.class)),
    AUTHOR(EnumSet.allOf(AccessOperation.class));

    private final Set<AccessOperation> grants;

    public boolean isSufficientFor(AccessLevel level) {
        return this.ordinal() >= level.ordinal();
    }

    public boolean allows(AccessOperation operation) {
        return grants.contains(operation);
    }
}
