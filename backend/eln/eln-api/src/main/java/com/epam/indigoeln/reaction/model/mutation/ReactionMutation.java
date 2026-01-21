package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReactionMutation extends Mutation {

    ReactionAnchor anchor();

    record SetScheme (
        @NotNull ReactionAnchor anchor,
        @Nullable String rxnFile,
        @NotNull List<InputAnchor> createdReactantAnchors,
        @NotNull List<InputSampleAnchor> createdReactantSampleAnchors,
        @NotNull List<InputAnchor> createdCatalystAnchors,
        @NotNull List<InputSampleAnchor> createdCatalystSampleAnchors,
        @NotNull List<OutputAnchor> createdProductAnchors
    ) implements ReactionMutation {

        @Override
        public String toString() {
            return "SetScheme[" +
                    "anchor=" + anchor +
                    ']';
        }
    }

    record UndoSetScheme (
            @NotNull ReactionAnchor anchor,
            @Nullable String rxnFile,
            @NotNull List<ReactionInput> inputs,
            @NotNull List<ReactionOutput> outputs
    ) implements ReactionMutation {
    }

    record ResolveInputs (
            @NotNull ReactionAnchor anchor,
            @NotEmpty Map<InputAnchor, UUID> inputSamples, // anchor -> sampleID
            @NotEmpty Map<InputAnchor, InputSampleAnchor> createdSampleAnchors
    ) implements ReactionMutation {
    }

    record UndoResolveInputs (
            @NotNull ReactionAnchor anchor,
            @NotNull Map<InputAnchor, RowUndo> rows
    ) implements ReactionMutation {
        public record RowUndo (
                @NotNull CompoundRef compoundRef,
                @NotNull List<ReactionInputSample> samples,
                @Nullable String chemicalName) {
        }
    }

    record AddEmptyInput (
        @NotNull ReactionAnchor anchor,
        @NotNull InputAnchor createdInputAnchor,
        @NotNull InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddEmptyInput(@NotNull ReactionAnchor anchor) {
            this(anchor, new InputAnchor(UUID.randomUUID()), new InputSampleAnchor(UUID.randomUUID()));
        }
    }

    record AddInput (
        @NotNull ReactionAnchor anchor,
        @NotNull UUID sampleId,
        @NotNull InputAnchor createdInputAnchor,
        @NotNull InputSampleAnchor createdSampleAnchor
    ) implements ReactionMutation {
        public AddInput(@NotNull ReactionAnchor anchor, @NotNull UUID sampleId) {
            this(anchor, sampleId, new InputAnchor(UUID.randomUUID()), new InputSampleAnchor(UUID.randomUUID()));
        }
    }

    record UndoRemoveInput (
        @NotNull ReactionAnchor anchor,
        @NotNull ReactionInput input,
        @NotNull Integer position,
        @NotNull InputAnchor limitingInput
    ) implements ReactionMutation {
    }
}
