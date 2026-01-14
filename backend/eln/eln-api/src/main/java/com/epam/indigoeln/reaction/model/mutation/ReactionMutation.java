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
        @Nullable String molFile
    ) implements ReactionMutation {

        @Override
        public String toString() {
            return "SetScheme[" +
                    "anchor=" + anchor +
                    ']';
        }
    }

    record ResolveInputs (
            @NotNull ReactionAnchor anchor,
            @NotEmpty Map<InputAnchor, UUID> inputSamples // anchor -> sampleID
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
        @NotNull ReactionAnchor anchor
    ) implements ReactionMutation {
    }

    record AddInput (
        @NotNull ReactionAnchor anchor,
        @NotNull UUID sampleId
    ) implements ReactionMutation {
    }

    record UndoRemoveInput (
        @NotNull ReactionAnchor anchor,
        @NotNull ReactionInput input,
        @NotNull Integer position,
        @NotNull InputAnchor limitingInput
    ) implements ReactionMutation {
    }
}
