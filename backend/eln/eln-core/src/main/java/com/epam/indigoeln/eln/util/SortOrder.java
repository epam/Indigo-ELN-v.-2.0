package com.epam.indigoeln.eln.util;

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

    public static SortOrder fromString(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        for (SortOrder order : SortOrder.values()) {
            if (order.value.equalsIgnoreCase(value)) {
                return order;
            }
        }
        throw new IllegalArgumentException("Invalid sort order: " + value + ". Valid values are: Earliest, Latest, Default.");
    }
}