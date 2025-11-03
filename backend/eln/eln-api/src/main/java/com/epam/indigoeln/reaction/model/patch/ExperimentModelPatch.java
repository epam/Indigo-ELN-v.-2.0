package com.epam.indigoeln.reaction.model.patch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExperimentModelPatch {

    @Nullable
    private Optional<Integer> lastUsedAnchor;

    @Nullable
    private Optional<Map<Integer, ReactionPatch>> reactions;
}
