package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionInputMutation extends Mutation permits
        ReactionInputMutation.SetInputRole,
        ReactionInputMutation.SetInputMol,
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

    record SetInputMol (
            @NotNull Anchor.Input anchor,
            @Nullable Double mol,
            @Nullable MolUnit molUnit
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

    @JsonTypeName("SetInputEQ")
    record SetInputEQ (
            @NotNull Anchor.Input anchor,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }
}
