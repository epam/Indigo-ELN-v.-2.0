package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionInputMutation extends Mutation permits
        ReactionInputMutation.SetInputRowRole,
        ReactionInputMutation.SetInputRowMol,
        ReactionInputMutation.SetInputRowLimiting,
        ReactionInputMutation.SetInputRowSaltCode,
        ReactionInputMutation.SetInputRowSaltEQ,
        ReactionInputMutation.SetInputRowEQ,
        ReactionInputMutation.SetInputCompoundStereoisomerCode,
        ReactionInputMutation.SetInputCompoundMolWeight
{

    Anchor.Input anchor();

    record SetInputRowRole(
            @NotNull Anchor.Input anchor,
            @NotNull ReactionRole role
    ) implements ReactionInputMutation {
    }

    record SetInputRowMol(
            @NotNull Anchor.Input anchor,
            @Nullable Double mol,
            @Nullable MolUnit molUnit
    ) implements ReactionInputMutation {
    }

    record SetInputRowLimiting(
            @NotNull Anchor.Input anchor
    ) implements ReactionInputMutation {
    }

    record SetInputRowSaltCode(
            @NotNull Anchor.Input anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputRowSaltEQ(
            @NotNull Anchor.Input anchor,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputRowEQ(
            @NotNull Anchor.Input anchor,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundStereoisomerCode(
            @NotNull Anchor.Input anchor,
            @Nullable DictionaryItemRef stereoisomerCode
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundMolWeight(
            @NotNull Anchor.Input anchor,
            @Nullable Double molWeight
    ) implements ReactionInputMutation {
    }
}
