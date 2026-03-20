package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;

public interface NotebookMutationHandler<T extends Mutation> extends MutationHandler<T, Void, NotebookEntity, NotebookSnapshot> {
}
