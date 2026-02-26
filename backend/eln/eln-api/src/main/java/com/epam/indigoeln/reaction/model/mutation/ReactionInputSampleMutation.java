package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ReactionInputSampleMutation extends Mutation {

    InputSampleAnchor anchor();

    record SetInputDensity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String density,
            @Nullable DensityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMolarity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String molarity,
            @Nullable MolarityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputVolume (
            @NotNull InputSampleAnchor anchor,
            @Nullable String volume,
            @Nullable VolumeUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputPurity (
            @NotNull InputSampleAnchor anchor,
            @Nullable String purity,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputHealthHazards (
            @NotNull InputSampleAnchor anchor,
            @NotNull List<DictionaryItemRef> healthHazards
    ) implements ReactionInputSampleMutation {
    }

    record SetInputMol (
            @NotNull InputSampleAnchor anchor,
            @Nullable String mol,
            @Nullable MolUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputWeight (
            @NotNull InputSampleAnchor anchor,
            @Nullable String weight,
            @Nullable WeightUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputSampleMutation {
    }

    record SetInputComment (
            @NotNull InputSampleAnchor anchor,
            @Nullable String comment
    ) implements ReactionInputSampleMutation {
    }
}
