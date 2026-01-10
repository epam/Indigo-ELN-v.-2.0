package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionInputPatch extends AbstractReactionRowPatch<Anchor.Input> {

    private Patched<Anchor.Input, Anchor.Input> anchor;

    @Nullable
    private Patched<ReactionRole, ReactionRole> role;

    @Nullable
    private Patched<EnteredValue<MolUnit>, EnteredValuePatch<MolUnit>> mol;

    @Nullable
    private Patched<String, String> chemicalName;

    @Nullable
    private Patched<List<ReactionInputSample>, ListPatch<ReactionInputSample, ReactionInputSamplePatch>> samples;

    @Nullable
    private Patched<Boolean, Boolean> limiting;
}
