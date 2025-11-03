package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactionOutputPatch extends AbstractReactionRowPatch<Anchor.Output> {

    @Nullable
    private Optional<String> chemicalName;

    @Nullable
    private Optional<ReactionOutputType> type;

    @Nullable
    private Optional<EnteredValuePatch<MolUnit>> theoMol;

    @Nullable
    private Optional<EnteredValuePatch<WeightUnit>> theoWeight;

    @Nullable
    private Optional<Map<Integer, ReactionOutputSamplePatch>> samples;
}
