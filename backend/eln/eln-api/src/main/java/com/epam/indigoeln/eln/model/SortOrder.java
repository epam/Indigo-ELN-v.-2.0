package com.epam.indigoeln.eln.model;

import lombok.Getter;

@Getter
public enum SortOrder {
    EARLIEST("Earliest"),
    LATEST("Latest"),
    DEFAULT("Default");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }
}