package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionOutputMutation extends Mutation permits
        ReactionOutputMutation.AddProductSample,
        ReactionOutputMutation.SetOutputRowType,
        ReactionOutputMutation.SetOutputRowSaltCode,
        ReactionOutputMutation.SetOutputRowSaltEQ,
        ReactionOutputMutation.SetOutputRowEQ,
        ReactionOutputMutation.SetOutputRowName,
        ReactionOutputMutation.SetOutputCompoundFormula,
        ReactionOutputMutation.SetOutputCompoundStereoisomerCode,
        ReactionOutputMutation.SetOutputCompoundMolWeight
{

    Anchor.Output anchor();

    record AddProductSample(
            @NotNull Anchor.Output anchor
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowType(
            @NotNull Anchor.Output anchor,
            @NotNull ReactionOutputType outputType
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltCode(
            @NotNull Anchor.Output anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltEQ(
            @NotNull Anchor.Output anchor,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowEQ(
            @NotNull Anchor.Output anchor,
            @Nullable Double eq
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowName(
            @NotNull Anchor.Output anchor,
            @NotNull String name
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundFormula(
            @NotNull Anchor.Output anchor,
            @Nullable String formula
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundStereoisomerCode(
            @NotNull Anchor.Output anchor,
            @Nullable DictionaryItemRef stereoisomerCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundMolWeight(
            @NotNull Anchor.Output anchor,
            @Nullable Double molWeight
    ) implements ReactionOutputMutation {
    }
}
