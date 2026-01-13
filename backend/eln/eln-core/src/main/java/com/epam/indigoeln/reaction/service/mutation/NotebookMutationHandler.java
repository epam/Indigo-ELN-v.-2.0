package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutationContext;
import org.jspecify.annotations.Nullable;

public interface NotebookMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler {

    MutationResult handle(NotebookEntity notebook, T mutation, @Nullable R redoInfo, NotebookMutationContext context);

    void initContext(NotebookEntity notebook, T mutation, NotebookMutationContext context);
}
