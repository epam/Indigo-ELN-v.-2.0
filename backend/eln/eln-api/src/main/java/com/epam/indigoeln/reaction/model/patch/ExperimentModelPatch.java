package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentModelPatch {

    @Nullable
    private Patched<List<Reaction>, ListPatch<Reaction, ReactionPatch>> reactions;
}
