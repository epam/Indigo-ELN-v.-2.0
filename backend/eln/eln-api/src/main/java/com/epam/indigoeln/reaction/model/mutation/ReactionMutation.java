package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.reaction.model.Anchor;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public sealed interface ReactionMutation extends Mutation permits
        ReactionMutation.SetScheme,
        ReactionMutation.ResolveInputs,
        ReactionMutation.AddEmptyInput,
        ReactionMutation.AddInput,
        ReactionMutation.RemoveInput
{

    Anchor.Reaction anchor();

    record SetScheme (
        @NotNull Anchor.Reaction anchor,
        @NotNull String molFile
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
        @NotNull Map<Anchor.Input, UUID> inputSamples // anchor -> sampleID
    ) implements ReactionMutation {
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

    record RemoveInput (
        @NotNull Anchor.Reaction anchor,
        @NotNull Anchor.Input input
    ) implements ReactionMutation {
    }
}
