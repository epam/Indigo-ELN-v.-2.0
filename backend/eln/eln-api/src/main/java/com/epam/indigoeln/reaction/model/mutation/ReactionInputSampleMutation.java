package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.units.*;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionInputSampleMutation extends ReactionInputMutation permits
        ReactionInputSampleMutation.SetInputDensity,
        ReactionInputSampleMutation.SetInputMolarity,
        ReactionInputSampleMutation.SetInputVolume,
        ReactionInputSampleMutation.SetInputPurity,
        ReactionInputSampleMutation.SetInputMol,
        ReactionInputSampleMutation.SetInputWeight {

    int sampleNo();

    record SetInputDensity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double purity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double mol,
            @Nullable MolUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double weight,
            @Nullable WeightUnit unit
    ) implements ReactionInputSampleMutation {
    }
}
