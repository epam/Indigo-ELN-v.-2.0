package com.epam.indigoeln.eln.model;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@RequiredArgsConstructor
public enum AccessLevel {
    NONE(EnumSet.noneOf(ApplicationPermission.class)),
    IMPLICIT_VIEW(EnumSet.of(
            ApplicationPermission.VIEW_PROJECTS,
            ApplicationPermission.VIEW_NOTEBOOKS,
            ApplicationPermission.VIEW_EXPERIMENTS
    )),
    VIEW(EnumSet.of(
            ApplicationPermission.VIEW_PROJECTS,
            ApplicationPermission.VIEW_NOTEBOOKS,
            ApplicationPermission.VIEW_EXPERIMENTS
    )),
    EDIT(EnumSet.of(
            ApplicationPermission.VIEW_PROJECTS,
            ApplicationPermission.CREATE_PROJECTS,
            ApplicationPermission.EDIT_PROJECTS,
            ApplicationPermission.DELETE_PROJECTS,
            ApplicationPermission.VIEW_NOTEBOOKS,
            ApplicationPermission.CREATE_NOTEBOOKS,
            ApplicationPermission.EDIT_NOTEBOOKS,
            ApplicationPermission.DELETE_NOTEBOOKS,
            ApplicationPermission.VIEW_EXPERIMENTS,
            ApplicationPermission.CREATE_EXPERIMENTS,
            ApplicationPermission.EDIT_EXPERIMENTS,
            ApplicationPermission.DELETE_EXPERIMENTS
    )),
    ADMIN(EnumSet.allOf(ApplicationPermission.class)),
    AUTHOR(EnumSet.allOf(ApplicationPermission.class));

    private final Set<ApplicationPermission> grants;

    public boolean isSufficientFor(AccessLevel level) {
        return this.ordinal() >= level.ordinal();
    }

    public boolean allows(ApplicationPermission operation) {
        return grants.contains(operation);
    }
}
