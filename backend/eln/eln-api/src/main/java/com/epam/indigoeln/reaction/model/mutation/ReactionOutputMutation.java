package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryRef;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public sealed interface ReactionOutputMutation extends ReactionMutation permits
        ReactionOutputSampleMutation,
        ReactionOutputMutation.AddProductSample,
        ReactionOutputMutation.SetOutputType,
        ReactionOutputMutation.SetOutputSaltCode,
        ReactionOutputMutation.SetOutputSaltEQ,
        ReactionOutputMutation.SetOutputEQ
{

    int rowNo();

    record AddProductSample(
            int reactionNo,
            int rowNo
    ) implements ReactionOutputMutation {
    }

    record SetOutputType(
            int reactionNo,
            int rowNo,
            @NotNull
            ReactionOutputType type
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltCode (
            int reactionNo,
            int rowNo,
            @Nullable
            DictionaryRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputSaltEQ (
            int reactionNo,
            int rowNo,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputEQ (
            int reactionNo,
            int rowNo,
            @Nullable Double eq
    ) implements ReactionOutputMutation {
    }
}
