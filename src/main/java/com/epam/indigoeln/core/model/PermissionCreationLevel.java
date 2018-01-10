package com.epam.indigoeln.core.model;

import lombok.AllArgsConstructor;

/**
 * Name of entity where user has been added to
 */
@AllArgsConstructor
public enum PermissionCreationLevel {

    PROJECT("Project"),
    NOTEBOOK("Notebook"),
    EXPERIMENT("Experiment");

    private final String value;
}
