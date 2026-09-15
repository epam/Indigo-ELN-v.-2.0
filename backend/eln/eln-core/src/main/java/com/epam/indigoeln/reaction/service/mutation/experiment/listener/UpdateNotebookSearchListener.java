package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.service.NotebookService;
import com.epam.indigoeln.eln.util.SearchVector;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.NotebookMutationListener;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateNotebookSearchListener implements NotebookMutationListener {

    @Inject
    NotebookService notebookService;
    @PersistenceContext
    EntityManager em;

    @Override
    public void beforePersist(NotebookEntity notebook, NotebookSnapshot snapshotBefore, NotebookSnapshot snapshotAfter) {
        // TODO detect from diff what parts potentially changed
        SearchVector oldSearchVector = notebookService.collectSearchVector(snapshotBefore);
        SearchVector newSearchVector = notebookService.collectSearchVector(snapshotAfter);
        boolean searchVectorChanged = !newSearchVector.equals(oldSearchVector);
        if (searchVectorChanged) {
            notebook.setSearchVector(newSearchVector);
        }
    }
}
