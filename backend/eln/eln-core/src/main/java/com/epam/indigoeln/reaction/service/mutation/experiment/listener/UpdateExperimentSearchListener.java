package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentSearchBatch;
import com.epam.indigoeln.eln.entity.ExperimentSearchCompound;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.util.SearchVector;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import one.util.streamex.StreamEx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateExperimentSearchListener implements ExperimentMutationListener {

    @Inject
    ExperimentService experimentService;
    @PersistenceContext
    EntityManager em;

    @Override
    public void beforePersist(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter) {
        // TODO detect from diff what parts potentially changed
        SearchVector oldSearchVector = experimentService.collectSearchVector(snapshotBefore);
        SearchVector newSearchVector = experimentService.collectSearchVector(snapshotAfter);
        if (!newSearchVector.equals(oldSearchVector)) {
            experiment.setSearchVector(newSearchVector);
        }
    }

    @Override
    public void afterUpdateEntity(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter) {
        Set<ExperimentSearchCompound> oldCompounds = collectCompoundRefs(snapshotBefore);
        Set<ExperimentSearchCompound> newCompounds = collectCompoundRefs(snapshotAfter);
        if (!oldCompounds.equals(newCompounds)) {
            experiment.setSearchCompounds(newCompounds);
        }

        List<String> oldRxnfiles = collectRxnfiles(snapshotBefore);
        List<String> newRxnfiles = collectRxnfiles(snapshotAfter);
        if (!oldRxnfiles.equals(newRxnfiles)) {
            experiment.setSearchRxnfiles(newRxnfiles);
        }

        Set<ExperimentSearchBatch> oldBatches = collectBatches(snapshotBefore);
        Set<ExperimentSearchBatch> newBatches = collectBatches(snapshotAfter);
        if (!oldBatches.equals(newBatches)) {
            experiment.setSearchBatches(newBatches);
        }
    }

    private Set<ExperimentSearchCompound> collectCompoundRefs(ExperimentSnapshot snapshot) {
        Set<ExperimentSearchCompound> refs = new HashSet<>();
        for (Reaction reaction : snapshot.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    refs.add(new ExperimentSearchCompound(input.getRole(), em.getReference(CompoundEntity.class, c.getCompoundID())));
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    refs.add(new ExperimentSearchCompound(ReactionRole.OUTPUT, em.getReference(CompoundEntity.class, c.getCompoundID())));
                }
            }
        }
        return refs;
    }

    private List<String> collectRxnfiles(ExperimentSnapshot snapshot) {
        return StreamEx.of(snapshot.getModel().getReactions())
                .map(Reaction::getRxnfile)
                .nonNull()
                .toList();
    }

    private Set<ExperimentSearchBatch> collectBatches(ExperimentSnapshot snapshot) {
        Set<ExperimentSearchBatch> set = new HashSet<>();
        for (Reaction reaction : snapshot.getModel().getReactions()) {
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    if (!sample.getPurity().isEmpty() || !sample.getYieldValue().isEmpty()) {
                        set.add(new ExperimentSearchBatch(sample.getPurity().getValueOrNull(), sample.getYieldValue().getValueOrNull()));
                    }
                }
            }
        }
        return set;
    }
}
