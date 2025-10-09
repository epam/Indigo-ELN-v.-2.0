package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface ReactionInputSampleMutation extends Mutation permits
        ReactionInputSampleMutation.SetInputDensity,
        ReactionInputSampleMutation.SetInputMolarity,
        ReactionInputSampleMutation.SetInputVolume,
        ReactionInputSampleMutation.SetInputPurity,
        ReactionInputSampleMutation.SetInputHealthHazards,
        ReactionInputSampleMutation.SetInputMol,
        ReactionInputSampleMutation.SetInputWeight {

    Anchor.InputSample anchor();

    record SetInputDensity (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double purity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputHealthHazards (
            @NotNull Anchor.InputSample anchor,
            @NotNull List<DictionaryItemRef> healthHazards
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double mol,
            @Nullable MolUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            @NotNull Anchor.InputSample anchor,
            @Nullable Double weight,
            @Nullable WeightUnit unit
    ) implements ReactionInputSampleMutation {
    }
}
