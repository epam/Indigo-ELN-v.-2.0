package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.service.NotebookService;
import com.epam.indigoeln.eln.util.SearchVectorField;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.NotebookMutationListener;
import com.epam.indigoeln.reaction.service.mutation.notebook.NotebookMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateNotebookSearchVectorListener implements NotebookMutationListener {

    @Inject
    NotebookService notebookService;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private List<@Nullable SearchVectorField> oldFields;

    @Override
    public void beforeHandle(NotebookEntity notebook, NotebookMutationContext context) {
        oldFields = notebookService.collectSearchFields(notebook);
    }

    @Override
    public void afterUpdateEntity(NotebookEntity notebook) {
        List<@Nullable SearchVectorField> newFields = notebookService.collectSearchFields(notebook);
        if (!newFields.equals(oldFields)) {
            notebookService.updateSearchVector(notebook, newFields);
        }
    }
}
