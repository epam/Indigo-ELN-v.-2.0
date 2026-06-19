package com.epam.indigoeln.reaction.service.mutation;

import com.fasterxml.jackson.databind.JsonNode;

public record MutationResult<S, C>(
        S snapshotBefore,
        S snapshotAfter,
        JsonNode patch,
        C context
) {
}
