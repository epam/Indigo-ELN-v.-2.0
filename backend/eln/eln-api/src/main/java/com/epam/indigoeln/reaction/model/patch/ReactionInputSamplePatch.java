package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionInputSamplePatch extends AbstractReactionSamplePatch<InputSampleAnchor> {

    private Patched<InputSampleAnchor, InputSampleAnchor> anchor;

    @Nullable
    private Patched<UUID, UUID> sampleId;

    @Nullable
    private Patched<EnteredValue<MolUnit>, EnteredValuePatch<MolUnit>> mol;

    @Nullable
    private Patched<EnteredValue<WeightUnit>, EnteredValuePatch<WeightUnit>> weight;

    @Nullable
    private Patched<String, String> comment;
}
