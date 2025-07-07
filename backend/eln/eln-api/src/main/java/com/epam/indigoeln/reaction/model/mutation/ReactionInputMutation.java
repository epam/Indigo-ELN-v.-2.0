package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ReactionInputRole;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public sealed interface ReactionInputMutation extends Mutation permits
        ReactionInputMutation.SetInputRole,
        ReactionInputMutation.SetLimiting,
        ReactionInputMutation.SetInputSaltCode,
        ReactionInputMutation.SetInputSaltEQ,
        ReactionInputMutation.SetInputEQ
{

    UUID anchor();

    record SetInputRole (
            @NotNull UUID anchor,
            @NotNull ReactionInputRole role
    ) implements ReactionInputMutation {
    }

    record SetLimiting (
            @NotNull UUID anchor
    ) implements ReactionInputMutation {
    }

    record SetInputSaltCode (
            @NotNull UUID anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputSaltEQ (
            @NotNull UUID anchor,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputEQ (
            @NotNull UUID anchor,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }
}
