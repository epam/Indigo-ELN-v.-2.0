package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactionInputPatch extends AbstractReactionRowPatch<Anchor.Input> {

    @Nullable
    private Optional<ReactionRole> role;

    @Nullable
    private Optional<EnteredValuePatch<MolUnit>> mol;

    @Nullable
    private Optional<String> chemicalName;

    @Nullable
    private Optional<ListPatch<Integer, ReactionInputSamplePatch>> samples;

    @Nullable
    private Optional<Boolean> limiting;
}
