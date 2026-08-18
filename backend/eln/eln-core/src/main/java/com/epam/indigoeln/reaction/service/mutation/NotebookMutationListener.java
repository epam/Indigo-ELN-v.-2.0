package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.reaction.service.mutation.notebook.NotebookMutationContext;

public interface NotebookMutationListener extends MutationListener<NotebookEntity, NotebookMutationContext> {
    
}
