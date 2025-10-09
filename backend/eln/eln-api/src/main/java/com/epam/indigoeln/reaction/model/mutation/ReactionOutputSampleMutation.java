package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.Anchor;
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
        ReactionOutputSampleMutation.SetOutputActualWeight,
        ReactionOutputSampleMutation.RegisterSample
{

    Anchor.OutputSample anchor();

    record SetOutputDensity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double purity
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double actualMol,
            @Nullable MolUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double actualWeight,
            @Nullable WeightUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record RegisterSample (
            @NotNull Anchor.OutputSample anchor
    ) implements ReactionOutputSampleMutation {
    }
}
