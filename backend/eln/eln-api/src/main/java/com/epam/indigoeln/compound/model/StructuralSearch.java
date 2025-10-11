package com.epam.indigoeln.compound.model;

public record StructuralSearch(
        Type type,
        String query
) {

    public enum Type {

        EXACT,
        SUBSTRUCTURE,
        SIMILARITY
    }
}
