package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.WithRevision;

public interface MutationListener<E extends WithRevision, C> {

    int DEFAULT_PRIORITY = 1000;
    int VALIDATION_PRIORITY = 100;

    default void beforeHandle(E entity, C context) {
    }

    default void afterUpdateEntity(E entity) {
    }
}
