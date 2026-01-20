package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;

public interface NotebookMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler<T, Void, R, NotebookEntity, NotebookSnapshot, NotebookPatch> {
}
