package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionPatch {

    private Patched<Anchor.Reaction> anchor;

    @Nullable
    private Patched<String> rxnfile;

    @Nullable
    private Patched<Integer> rxnVersion; // !!! not needed with patch approach, remove when frontend is switched

    @Nullable
    private Patched<ListPatch<ReactionInputPatch>> inputs;

    @Nullable
    private Patched<ListPatch<ReactionOutputPatch>> outputs;
}
