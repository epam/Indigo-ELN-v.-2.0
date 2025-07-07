package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public sealed interface ReactionOutputMutation extends Mutation permits
        ReactionOutputMutation.AddProductSample,
        ReactionOutputMutation.SetOutputType,
        ReactionOutputMutation.SetOutputSaltCode,
        ReactionOutputMutation.SetOutputSaltEQ,
        ReactionOutputMutation.SetOutputEQ
{

    UUID anchor();

    record AddProductSample(
            @NotNull UUID anchor
    ) implements ReactionOutputMutation {
    }

    record SetOutputType(
            @NotNull UUID anchor,
            @NotNull
            ReactionOutputType type
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltCode (
            @NotNull UUID anchor,
            @Nullable
            DictionaryItemRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltEQ (
            @NotNull UUID anchor,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputEQ (
            @NotNull UUID anchor,
            @Nullable Double eq
    ) implements ReactionOutputMutation {
    }
}
