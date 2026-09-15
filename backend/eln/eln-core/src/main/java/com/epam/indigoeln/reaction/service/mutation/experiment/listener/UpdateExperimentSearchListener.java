package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentSearchBatch;
import com.epam.indigoeln.eln.entity.ExperimentSearchCompound;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import com.epam.indigoeln.eln.util.SearchVector;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateExperimentSearchListener implements ExperimentMutationListener {

    @Inject
    GlobalSearchService globalSearchService;

    @Override
    public void beforePersist(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter) {
        // TODO detect from diff what parts potentially changed
        SearchVector oldSearchVector = globalSearchService.collectExperimentSearchVector(snapshotBefore);
        SearchVector newSearchVector = globalSearchService.collectExperimentSearchVector(snapshotAfter);
        if (!newSearchVector.equals(oldSearchVector)) {
            experiment.setSearchVector(newSearchVector);
        }
    }

    @Override
    public void afterUpdateEntity(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter) {
        Set<ExperimentSearchCompound> oldCompounds = globalSearchService.collectExperimentCompoundRefs(snapshotBefore);
        Set<ExperimentSearchCompound> newCompounds = globalSearchService.collectExperimentCompoundRefs(snapshotAfter);
        if (!oldCompounds.equals(newCompounds)) {
            experiment.setSearchCompounds(newCompounds);
        }

        List<String> oldRxnfiles = globalSearchService.collectExperimentRxnfiles(snapshotBefore);
        List<String> newRxnfiles = globalSearchService.collectExperimentRxnfiles(snapshotAfter);
        if (!oldRxnfiles.equals(newRxnfiles)) {
            experiment.setSearchRxnfiles(newRxnfiles);
        }

        Set<ExperimentSearchBatch> oldBatches = globalSearchService.collectExperimentBatches(snapshotBefore);
        Set<ExperimentSearchBatch> newBatches = globalSearchService.collectExperimentBatches(snapshotAfter);
        if (!oldBatches.equals(newBatches)) {
            experiment.setSearchBatches(newBatches);
        }
    }
}
