package com.epam.indigoeln.compound.model.search;

import jakarta.validation.constraints.NotNull;

public record StructuralSearch(
        @NotNull Type type,
        @NotNull String query
) {

    public enum Type {

        EXACT,
        SUBSTRUCTURE,
        SIMILARITY
    }
}
