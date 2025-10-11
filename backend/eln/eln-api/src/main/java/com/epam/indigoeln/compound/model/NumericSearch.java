package com.epam.indigoeln.compound.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericSearch.Equals.class, name = "eq"),
        @JsonSubTypes.Type(value = NumericSearch.LessThenOrEqual.class, name = "le"),
        @JsonSubTypes.Type(value = NumericSearch.GreaterThenOrEqual.class, name = "ge")
})
public sealed interface NumericSearch permits NumericSearch.Equals, NumericSearch.LessThenOrEqual, NumericSearch.GreaterThenOrEqual {

    record Equals(
            double value
    ) implements NumericSearch {
    }

    record LessThenOrEqual(
            double value
    ) implements NumericSearch {
    }

    record GreaterThenOrEqual(
            double value
    ) implements NumericSearch {
    }
}
