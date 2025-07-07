package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public sealed interface ReactionOutputSampleMutation extends Mutation permits
        ReactionOutputSampleMutation.SetOutputDensity,
        ReactionOutputSampleMutation.SetOutputMolarity,
        ReactionOutputSampleMutation.SetOutputVolume,
        ReactionOutputSampleMutation.SetOutputPurity,
        ReactionOutputSampleMutation.SetOutputActualMol,
        ReactionOutputSampleMutation.SetOutputActualWeight
{

    UUID anchor();

    record SetOutputDensity (
            @NotNull UUID anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            @NotNull UUID anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            @NotNull UUID anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            @NotNull UUID anchor,
            @Nullable Double purity
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            @NotNull UUID anchor,
            @Nullable Double actualMol,
            @Nullable MolUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            @NotNull UUID anchor,
            @Nullable Double actualWeight,
            @Nullable WeightUnit unit
    ) implements ReactionOutputSampleMutation {
    }
}
