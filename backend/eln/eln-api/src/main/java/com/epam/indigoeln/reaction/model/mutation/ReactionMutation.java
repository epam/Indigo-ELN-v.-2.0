package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public sealed interface ReactionMutation extends Mutation permits
        ReactionMutation.SetScheme,
        ReactionMutation.ResolveInputs,
        ReactionMutation.UndoResolveInputs,
        ReactionMutation.AddEmptyInput,
        ReactionMutation.AddInput,
        ReactionMutation.UndoRemoveInput
{

    Anchor.Reaction anchor();

    record SetScheme (
        @NotNull Anchor.Reaction anchor,
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
            @NotNull Anchor.Reaction anchor,
            @NotEmpty Map<Anchor.Input, UUID> inputSamples // anchor -> sampleID
    ) implements ReactionMutation {
    }

    record UndoResolveInputs (
            @NotNull Anchor.Reaction anchor,
            @NotNull Map<Anchor.Input, RowUndo> rows
    ) implements ReactionMutation {
        public record RowUndo (
                @NotNull CompoundRef compoundRef,
                @NotNull List<ReactionInputSample> samples,
                @Nullable String chemicalName) {
        }
    }

    record AddEmptyInput (
        @NotNull Anchor.Reaction anchor
    ) implements ReactionMutation {
    }

    record AddInput (
        @NotNull Anchor.Reaction anchor,
        @NotNull UUID sampleId
    ) implements ReactionMutation {
    }

    record UndoRemoveInput (
        @NotNull Anchor.Reaction anchor,
        @NotNull ReactionInput input,
        @NotNull Anchor.Input limitingInput
    ) implements ReactionMutation {
    }
}
