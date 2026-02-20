package com.epam.indigoeln.compound.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericSearch.Equals.class, name = "eq"),
        @JsonSubTypes.Type(value = NumericSearch.LessThanOrEqual.class, name = "le"),
        @JsonSubTypes.Type(value = NumericSearch.GreaterThanOrEqual.class, name = "ge")
})
public sealed interface NumericSearch permits NumericSearch.Equals, NumericSearch.LessThanOrEqual, NumericSearch.GreaterThanOrEqual {

    record Equals(
            @NotNull Double value
    ) implements NumericSearch {
    }

    record LessThanOrEqual(
            @NotNull Double value
    ) implements NumericSearch {
    }

    record GreaterThanOrEqual(
            @NotNull Double value
    ) implements NumericSearch {
    }

    Double value();

    default String operator() {
        return switch (this) {
            case Equals eq -> "=";
            case GreaterThanOrEqual ge -> ">=";
            case LessThanOrEqual le -> "<=";
        };
    }
}
