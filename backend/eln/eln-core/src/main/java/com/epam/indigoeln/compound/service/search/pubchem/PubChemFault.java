package com.epam.indigoeln.compound.service.search.pubchem;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jspecify.annotations.Nullable;

import java.util.List;

@RegisterForReflection
record PubChemFault(
        @Nullable @JsonProperty("Fault") Fault fault
) {

    @RegisterForReflection
    record Fault(
            @Nullable @JsonProperty("Code") String code,
            @Nullable @JsonProperty("Message") String message,
            @Nullable @JsonProperty("Details") List<String> details
    ) {
    }
}

