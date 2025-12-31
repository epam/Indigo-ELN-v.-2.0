package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentModelPatch {

    @Nullable
    private Patched<Integer> lastUsedAnchor;

    @Nullable
    private Patched<ListPatch<ReactionPatch>> reactions;
}
