package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public interface ReactionInputMutation extends Mutation {

    InputAnchor anchor();

    record SetInputRowRole(
            @NotNull InputAnchor anchor,
            @NotNull ReactionRole role
    ) implements ReactionInputMutation {
    }

    record SetInputRowMol(
            @NotNull InputAnchor anchor,
            @Nullable Double mol,
            @Nullable MolUnit molUnit,
            @Nullable EnteredValueSource source
    ) implements ReactionInputMutation {
    }

    record SetInputRowChemicalName(
            @NotNull InputAnchor anchor,
            @Nullable String chemicalName
    ) implements ReactionInputMutation {
    }

    record SetInputRowLimiting(
            @NotNull InputAnchor anchor
    ) implements ReactionInputMutation {
    }

    record SetInputRowSaltCode(
            @NotNull InputAnchor anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputRowSaltEQ(
            @NotNull InputAnchor anchor,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputRowEQ(
            @NotNull InputAnchor anchor,
            @Nullable Double eq
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundStereoisomerCode(
            @NotNull InputAnchor anchor,
            @Nullable DictionaryItemRef stereoisomerCode
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundMolWeight(
            @NotNull InputAnchor anchor,
            @Nullable Double molWeight,
            @Nullable EnteredValueSource source
    ) implements ReactionInputMutation {
    }

    record RemoveInput (
            @NotNull InputAnchor anchor
    ) implements ReactionInputMutation {
    }
}
