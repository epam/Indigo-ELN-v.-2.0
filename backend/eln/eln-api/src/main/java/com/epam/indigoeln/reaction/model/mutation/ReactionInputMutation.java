package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
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

    Anchor.Input anchor();

    record SetInputRole (
            @NotNull Anchor.Input anchor,
            @NotNull ReactionRole role
    ) implements ReactionInputMutation {
    }

    record SetLimiting (
            @NotNull Anchor.Input anchor
    ) implements ReactionInputMutation {
    }

    record SetInputSaltCode (
            @NotNull Anchor.Input anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputSaltEQ (
            @NotNull Anchor.Input anchor,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputEQ (
            @NotNull Anchor.Input anchor,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }
}
