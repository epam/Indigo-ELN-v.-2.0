package com.epam.indigoeln.reaction.model.mutation;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public sealed interface ReactionMutation extends Mutation permits
        ReactionMutation.SetScheme,
        ReactionMutation.ResolveInputs,
        ReactionMutation.AddEmptyInput,
        ReactionMutation.RemoveInput
{

    UUID anchor();

    record SetScheme (
        @NotNull UUID anchor,
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
        @NotNull UUID anchor,
        @NotNull Map<UUID, UUID> inputSamples // anchor -> sampleID
    ) implements ReactionMutation {
    }

    record AddEmptyInput (
        @NotNull UUID anchor
    ) implements ReactionMutation {
    }

    record RemoveInput (
        @NotNull UUID anchor,
        @NotNull UUID inputRow
    ) implements ReactionMutation {
    }
}
