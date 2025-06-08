package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.units.*;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionOutputSampleMutation extends ReactionOutputMutation permits
        ReactionOutputSampleMutation.SetOutputDensity,
        ReactionOutputSampleMutation.SetOutputMolarity,
        ReactionOutputSampleMutation.SetOutputVolume,
        ReactionOutputSampleMutation.SetOutputPurity,
        ReactionOutputSampleMutation.SetOutputActualMol,
        ReactionOutputSampleMutation.SetOutputActualWeight
{

    int sampleNo();

    record SetOutputDensity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double purity
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double actualMol,
            @Nullable MolUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            int reactionNo,
            int rowNo,
            int sampleNo,
            @Nullable Double actualWeight,
            @Nullable WeightUnit unit
    ) implements ReactionOutputSampleMutation {
    }
}
