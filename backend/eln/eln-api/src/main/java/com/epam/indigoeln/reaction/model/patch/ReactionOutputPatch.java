package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionOutputPatch extends AbstractReactionRowPatch<OutputAnchor> {

    private Patched<OutputAnchor, OutputAnchor> anchor;

    @Nullable
    private Patched<String, String> outputName;

    @Nullable
    private Patched<ReactionOutputType, ReactionOutputType> type;

    @Nullable
    private Patched<EnteredValue<MolUnit>, EnteredValuePatch<MolUnit>> theoMol;

    @Nullable
    private Patched<EnteredValue<WeightUnit>, EnteredValuePatch<WeightUnit>> theoWeight;

    @Nullable
    private Patched<List<ReactionOutputSample>, ListPatch<ReactionOutputSample, ReactionOutputSamplePatch>> samples;
}
