package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

public interface MutationHandler<T, M, E extends WithRevision, S> {

    MutationResult doHandle(E entity, @Nullable M model, T mutation);
    Pair<S, JsonNode> applyMutation(E entity, T mutation);
}
