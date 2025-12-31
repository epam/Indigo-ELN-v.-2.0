package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionInputPatch extends AbstractReactionRowPatch<Anchor.Input> {

    private Patched<Anchor.Input> anchor;

    @Nullable
    private Patched<ReactionRole> role;

    @Nullable
    private Patched<EnteredValuePatch<MolUnit>> mol;

    @Nullable
    private Patched<String> chemicalName;

    @Nullable
    private Patched<ListPatch<ReactionInputSamplePatch>> samples;

    @Nullable
    private Patched<Boolean> limiting;
}
