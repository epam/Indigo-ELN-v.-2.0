package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import org.jspecify.annotations.Nullable;

public record MutationResult (
    String summary,
    @Nullable MutationRedoInfo redoInfo,
    @Nullable Mutation reverseMutation
) {

    public MutationResult(String summary) {
        this(summary, null, null);
    }
}
