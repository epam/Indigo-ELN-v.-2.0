package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.WithRevision;
import org.jspecify.annotations.Nullable;

public interface MutationHandler<T, M, R, E extends WithRevision, S, P> {

    MutationResult doHandle(E entity, @Nullable M model, T mutation, @Nullable R redoInfo);
    Pair<S, P> applyMutation(E entity, T mutation);
}
