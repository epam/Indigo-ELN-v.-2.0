package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionOutputMutation extends Mutation permits
        ReactionOutputMutation.AddProductSample,
        ReactionOutputMutation.SetOutputType,
        ReactionOutputMutation.SetOutputSaltCode,
        ReactionOutputMutation.SetOutputSaltEQ,
        ReactionOutputMutation.SetOutputEQ
{

    Anchor.Output anchor();

    record AddProductSample(
            @NotNull Anchor.Output anchor
    ) implements ReactionOutputMutation {
    }

    record SetOutputType(
            @NotNull Anchor.Output anchor,
            @NotNull ReactionOutputType outputType
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltCode (
            @NotNull Anchor.Output anchor,
            @Nullable DictionaryItemRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltEQ (
            @NotNull Anchor.Output anchor,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputEQ (
            @NotNull Anchor.Output anchor,
            @Nullable Double eq
    ) implements ReactionOutputMutation {
    }
}
