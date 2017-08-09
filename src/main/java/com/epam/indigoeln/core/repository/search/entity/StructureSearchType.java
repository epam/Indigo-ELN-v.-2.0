package com.epam.indigoeln.core.repository.search.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StructureSearchType {

    PRODUCT("Product"),
    REACTION("Reaction");

    private final String value;

    StructureSearchType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static StructureSearchType fromValue(String value) {
        for (StructureSearchType type : StructureSearchType.values()) {
            if (type.toString().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
