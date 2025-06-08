package com.epam.indigoeln.eln.model;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@RequiredArgsConstructor
public enum AccessLevel {
    NONE(EnumSet.noneOf(AccessOperation.class)),
    IMPLICIT_VIEW(EnumSet.of(
            AccessOperation.VIEW,
            AccessOperation.LIST_NOTEBOOKS,
            AccessOperation.LIST_EXPERIMENTS,
            AccessOperation.MARK_EXPERIMENT
    )),
    VIEW(EnumSet.of(
            AccessOperation.VIEW,
            AccessOperation.LIST_NOTEBOOKS,
            AccessOperation.LIST_EXPERIMENTS,
            AccessOperation.MARK_EXPERIMENT
    )),
    EDIT(EnumSet.of(
            AccessOperation.VIEW,
            AccessOperation.EDIT,
            AccessOperation.MANAGE_ATTACHMENTS,
            AccessOperation.LIST_NOTEBOOKS,
            AccessOperation.CREATE_NOTEBOOK,
            AccessOperation.LIST_EXPERIMENTS,
            AccessOperation.CREATE_EXPERIMENT,
            AccessOperation.MARK_EXPERIMENT
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
