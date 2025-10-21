package com.epam.indigoeln.compound.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericSearch.Equals.class, name = "eq"),
        @JsonSubTypes.Type(value = NumericSearch.LessThanOrEqual.class, name = "le"),
        @JsonSubTypes.Type(value = NumericSearch.GreaterThanOrEqual.class, name = "ge")
})
public sealed interface NumericSearch permits NumericSearch.Equals, NumericSearch.LessThanOrEqual, NumericSearch.GreaterThanOrEqual {

    record Equals(
            double value
    ) implements NumericSearch {
    }

    record LessThanOrEqual(
            double value
    ) implements NumericSearch {
    }

    record GreaterThanOrEqual(
            double value
    ) implements NumericSearch {
    }
}
