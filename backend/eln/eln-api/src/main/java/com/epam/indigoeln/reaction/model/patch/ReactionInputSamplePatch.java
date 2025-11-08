package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactionInputSamplePatch extends AbstractReactionSamplePatch<Anchor.InputSample> {

    @Nullable
    private Optional<UUID> sampleId;

    @Nullable
    private Optional<String> chemicalName;

    @Nullable
    private Optional<EnteredValuePatch<MolUnit>> mol;

    @Nullable
    private Optional<EnteredValuePatch<WeightUnit>> weight;

    @Nullable
    private Optional<String> comment;
}
