package com.epam.indigoeln.compound.model;

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
public sealed interface TextSearch permits TextSearch.ExactSearch, TextSearch.StartsWithSearch, TextSearch.ContainsSearch, TextSearch.EndsWithSearch, TextSearch.BetweenSearch {

    record ExactSearch(
            @NotNull String value
    ) implements TextSearch {
    }

    record StartsWithSearch(
            String value
    ) implements TextSearch {
    }

    record ContainsSearch(
            String value
    ) implements TextSearch {
    }

    record EndsWithSearch(
            String value
    ) implements TextSearch {
    }

    record BetweenSearch(
            String from,
            String to
    ) implements TextSearch {
    }
}
