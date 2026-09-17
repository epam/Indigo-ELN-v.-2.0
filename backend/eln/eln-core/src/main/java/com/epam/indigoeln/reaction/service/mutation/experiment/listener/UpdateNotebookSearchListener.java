package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import com.epam.indigoeln.eln.util.SearchVector;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.NotebookMutationListener;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateNotebookSearchListener implements NotebookMutationListener {

    @Inject
    GlobalSearchService globalSearchService;

    @Override
    public void beforePersist(NotebookEntity notebook, NotebookSnapshot snapshotBefore, NotebookSnapshot snapshotAfter) {
        // TODO detect from diff what parts potentially changed
        SearchVector oldSearchVector = globalSearchService.collectNotebookSearchVector(snapshotBefore);
        SearchVector newSearchVector = globalSearchService.collectNotebookSearchVector(snapshotAfter);
        boolean searchVectorChanged = !newSearchVector.equals(oldSearchVector);
        if (searchVectorChanged) {
            notebook.setSearchVector(newSearchVector);
        }
    }
}
