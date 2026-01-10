package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public interface ReactionOutputMutation extends Mutation {

    OutputAnchor anchor();

    record AddProductSample(
            @NotNull OutputAnchor anchor
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowType(
            @NotNull OutputAnchor anchor,
            @NotNull ReactionOutputType outputType
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltCode(
            @NotNull OutputAnchor anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltEQ(
            @NotNull OutputAnchor anchor,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowEQ(
            @NotNull OutputAnchor anchor,
            @Nullable Double eq
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowName(
            @NotNull OutputAnchor anchor,
            @NotNull String name
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundStereoisomerCode(
            @NotNull OutputAnchor anchor,
            @Nullable DictionaryItemRef stereoisomerCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundMolWeight(
            @NotNull OutputAnchor anchor,
            @Nullable Double molWeight,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputMutation {
    }

    record UndoRemoveProductSample(
            @NotNull OutputAnchor anchor,
            @NotNull ReactionOutputSample sample
    ) implements ReactionOutputMutation {
    }
}
