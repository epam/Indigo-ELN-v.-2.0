package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionPatch {

    private Patched<ReactionAnchor, ReactionAnchor> anchor;

    @Nullable
    private Patched<String, String> rxnfile;

    @Nullable
    private Patched<Integer, Integer> rxnVersion;

    @Nullable
    private Patched<List<ReactionInput>, ListPatch<ReactionInput, ReactionInputPatch>> inputs;

    @Nullable
    private Patched<List<ReactionOutput>, ListPatch<ReactionOutput, ReactionOutputPatch>> outputs;

    @Nullable
    private Patched<List<STRCodeSample>, List<STRCodeSample>> precursorReactantIds;
}
