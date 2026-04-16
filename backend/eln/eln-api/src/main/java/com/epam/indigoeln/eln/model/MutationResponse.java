package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class MutationResponse {

    @NotNull
    JsonNode patch;

    @NotNull
    ExperimentSnapshot updated;

    @Nullable
    Map<InputAnchor, String> unresolvedInputs;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<String> messages = new ArrayList<>();
}
