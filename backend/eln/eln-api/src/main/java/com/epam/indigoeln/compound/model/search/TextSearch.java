package com.epam.indigoeln.compound.model.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextSearch.ExactSearch.class, name = "exact"),
        @JsonSubTypes.Type(value = TextSearch.StartsWithSearch.class, name = "startsWith"),
        @JsonSubTypes.Type(value = TextSearch.ContainsSearch.class, name = "contains"),
        @JsonSubTypes.Type(value = TextSearch.EndsWithSearch.class, name = "endsWith"),
        @JsonSubTypes.Type(value = TextSearch.BetweenSearch.class, name = "between")
})
public sealed interface TextSearch permits TextSearch.WithValue, TextSearch.BetweenSearch {

    sealed interface WithValue extends TextSearch permits ExactSearch, StartsWithSearch, ContainsSearch, EndsWithSearch {

        String value();
    }

    record ExactSearch(
            @NotNull String value
    ) implements WithValue {
    }

    record StartsWithSearch(
            String value
    ) implements WithValue {
    }

    record ContainsSearch(
            String value
    ) implements WithValue {
    }

    record EndsWithSearch(
            String value
    ) implements WithValue {
    }

    record BetweenSearch(
            String from,
            String to
    ) implements TextSearch {
    }
}
