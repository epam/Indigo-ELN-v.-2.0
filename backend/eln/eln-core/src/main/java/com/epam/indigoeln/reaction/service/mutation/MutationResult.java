package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import org.jspecify.annotations.Nullable;

public record MutationResult (
    String summary,
    @Nullable Mutation reverseMutation
) {
}
