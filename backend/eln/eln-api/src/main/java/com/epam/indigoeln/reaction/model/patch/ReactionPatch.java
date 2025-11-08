package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactionPatch extends AbstractListElementPatch<Anchor.Reaction> {

    @Nullable
    private Optional<String> rxnfile;

    @Nullable
    private Optional<Integer> rxnVersion; // TODO not needed with patch approach, remove when frontend is switched

    @Nullable
    private Optional<Map<Integer, ReactionInputPatch>> inputs;

    @Nullable
    private Optional<Map<Integer, ReactionOutputPatch>> outputs;
}
