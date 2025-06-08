package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryRef;
import com.epam.indigoeln.reaction.model.ReactionInputRole;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionInputMutation extends ReactionMutation permits
        ReactionInputSampleMutation,
        ReactionInputMutation.SetInputRole,
        ReactionInputMutation.SetLimiting,
        ReactionInputMutation.SetInputSaltCode,
        ReactionInputMutation.SetInputSaltEQ,
        ReactionInputMutation.SetInputEQ
{

    int rowNo();

    record SetInputRole (
            int reactionNo,
            int rowNo,
            @NotNull
            ReactionInputRole role
    ) implements ReactionInputMutation {
    }

    record SetLimiting (
            int reactionNo,
            int rowNo
    ) implements ReactionInputMutation {
    }

    record SetInputSaltCode (
            int reactionNo,
            int rowNo,
            @Nullable
            DictionaryRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputSaltEQ (
            int reactionNo,
            int rowNo,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputEQ (
            int reactionNo,
            int rowNo,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }
}
