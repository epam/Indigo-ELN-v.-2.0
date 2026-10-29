package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface ReactionInputSampleMutation extends ExperimentMutation {

    InputSampleAnchor anchor();

    @Override
    default boolean isApplicableToEditSession() {
        return true;
    }

    record SetInputDensity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String density
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String molarity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            @NotNull InputSampleAnchor anchor,
            @Nullable String volume
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String purity
    ) implements ReactionInputSampleMutation {
    }

    record SetInputHealthHazards (
            @NotNull InputSampleAnchor anchor,
            @NotNull Set<HealthHazardRef> healthHazards
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            @NotNull InputSampleAnchor anchor,
            @Nullable String mol
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            @NotNull InputSampleAnchor anchor,
            @Nullable String weight
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
