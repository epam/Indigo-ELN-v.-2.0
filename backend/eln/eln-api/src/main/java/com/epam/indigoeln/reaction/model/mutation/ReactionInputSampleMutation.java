package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ReactionInputSampleMutation extends ExperimentMutation {

    InputSampleAnchor anchor();

    record SetInputDensity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String density,
            @Nullable DensityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            @NotNull InputSampleAnchor anchor,
            @Nullable String volume,
            @Nullable VolumeUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String purity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputHealthHazards (
            @NotNull InputSampleAnchor anchor,
            @NotNull List<HealthHazardRef> healthHazards
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            @NotNull InputSampleAnchor anchor,
            @Nullable String mol,
            @Nullable MolUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            @NotNull InputSampleAnchor anchor,
            @Nullable String weight,
            @Nullable WeightUnit unit
    ) implements ReactionInputSampleMutation {
    }

    record SetInputComment (
            @NotNull InputSampleAnchor anchor,
            @Nullable String comment
    ) implements ReactionInputSampleMutation {
    }

    record RemoveInput (
            @NotNull InputSampleAnchor anchor
    ) implements ReactionInputSampleMutation {
    }
}
