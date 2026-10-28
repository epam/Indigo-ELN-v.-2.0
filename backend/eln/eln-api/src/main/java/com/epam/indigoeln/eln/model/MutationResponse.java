package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.InputAnchor;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MutationResponse {

    @NotNull
    JsonNode patch;

    @NotNull
    Map<InputAnchor, String> unresolvedInputs = new HashMap<>();

    @NotNull
    List<String> messages = new ArrayList<>();

    /**
     * User-entered values the recalculation replaced to resolve a conflict: {@code <row or sample anchor>:<property name>}
     * to why it was replaced.
     */
    @NotNull
    Map<String, String> conflicts = new LinkedHashMap<>();
}
