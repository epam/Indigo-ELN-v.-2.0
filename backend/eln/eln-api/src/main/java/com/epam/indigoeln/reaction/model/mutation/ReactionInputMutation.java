package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public interface ReactionInputMutation extends ExperimentMutation {

    InputAnchor anchor();

    @Override
    default boolean isApplicableToEditSession() {
        return true;
    }

    record SetInputRowRole(
            @NotNull InputAnchor anchor,
            @NotNull ReactionRole role
    ) implements ReactionInputMutation {
    }

    record SetInputRowMol(
            @NotNull InputAnchor anchor,
            @Nullable String mol,
            @Nullable MolUnit molUnit
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
            @Nullable SaltCodeRef saltCode
    ) implements ReactionInputMutation {
    }

    record SetInputRowSaltEQ(
            @NotNull InputAnchor anchor,
            @Nullable Double saltEQ
    ) implements ReactionInputMutation {
    }

    record SetInputRowEQ(
            @NotNull InputAnchor anchor,
            @Nullable String eq
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundStereoisomerCode(
            @NotNull InputAnchor anchor,
            @Nullable StereoisomerCodeRef stereoisomerCode
    ) implements ReactionInputMutation {
    }

    record SetInputCompoundMolWeight(
            @NotNull InputAnchor anchor,
            @Nullable String molWeight
    ) implements ReactionInputMutation {
    }

    record RemoveInputRow (
            @NotNull InputAnchor anchor
    ) implements ReactionInputMutation {
    }
}
