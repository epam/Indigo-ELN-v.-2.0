package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionOutputPatch extends AbstractReactionRowPatch<Anchor.Output> {

    private Patched<Anchor.Output> anchor;

    @Nullable
    private Patched<String> outputName;

    @Nullable
    private Patched<ReactionOutputType> type;

    @Nullable
    private Patched<EnteredValuePatch<MolUnit>> theoMol;

    @Nullable
    private Patched<EnteredValuePatch<WeightUnit>> theoWeight;

    @Nullable
    private Patched<ListPatch<ReactionOutputSamplePatch>> samples;
}
