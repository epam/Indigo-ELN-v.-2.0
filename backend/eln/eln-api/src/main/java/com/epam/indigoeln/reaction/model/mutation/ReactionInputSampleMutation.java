package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public sealed interface ReactionInputSampleMutation extends Mutation permits
        ReactionInputSampleMutation.SetInputDensity,
        ReactionInputSampleMutation.SetInputMolarity,
        ReactionInputSampleMutation.SetInputVolume,
        ReactionInputSampleMutation.SetInputPurity,
        ReactionInputSampleMutation.SetInputMol,
        ReactionInputSampleMutation.SetInputWeight {

    UUID anchor();

    record SetInputDensity (
            @NotNull UUID anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            @NotNull UUID anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            @NotNull UUID anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            @NotNull UUID anchor,
            @Nullable Double purity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            @NotNull UUID anchor,
            @Nullable Double mol,
            @Nullable MolUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            @NotNull UUID anchor,
            @Nullable Double weight,
            @Nullable WeightUnit unit
    ) implements ReactionInputSampleMutation {
    }
}
