package com.epam.indigoeln.reaction.model.mutation;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public sealed interface ReactionMutation extends Mutation permits
        ReactionInputMutation,
        ReactionOutputMutation,
        ReactionMutation.SetScheme,
        ReactionMutation.ResolveInputs
{

    int reactionNo();

    record SetScheme (
        int reactionNo,
        @NotNull
        String molFile
    ) implements ReactionMutation {

        @Override
        public String toString() {
            return "SetScheme[" +
                    "reactionNo=" + reactionNo +
                    ']';
        }
    }

    record ResolveInputs(
            int reactionNo,
            @NotNull
            Map<Integer, UUID> inputSamples
    ) implements ReactionMutation {
    }
}
