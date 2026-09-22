package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.eln.common.util.SearchVector;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentSearchBatch;
import com.epam.indigoeln.eln.entity.ExperimentSearchCompound;
import com.epam.indigoeln.eln.model.GlobalSearchRequest;
import com.epam.indigoeln.eln.model.GlobalSearchResultDTO;
import com.epam.indigoeln.eln.repository.GlobalSearchRepository;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import one.util.streamex.StreamEx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DataAccess
@Transactional
@ApplicationScoped
public class GlobalSearchService {

    @Inject
    GlobalSearchRepository globalSearchRepository;
    @PersistenceContext
    EntityManager em;

    public Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, Paging paging) {
        return globalSearchRepository.search(request, paging);
    }

    public SearchVector collectExperimentSearchVector(ExperimentSnapshot snapshot) {
        SearchVector.Builder sv = new SearchVector.Builder()
                .aIdentifier(snapshot.getName())
                .a(snapshot.getTitle())
                .d(snapshot.getDescription())
                .d(snapshot.getLiterature());
        for (Reaction reaction : snapshot.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                sv.bIdentifier(input.getCompound().getCompoundKey());
                for (ReactionInputSample sample : input.getSamples()) {
                    sv.cIdentifier(sample.getSampleKey());
                    sv.cIdentifier(sample.getNbkBatchNumber() != null ? sample.getNbkBatchNumber().toString() : null);
                }
            }
        }
        return sv.build();
    }

    public SearchVector collectProjectSearchVector(ProjectSnapshot snapshot) {
        SearchVector.Builder sv = new SearchVector.Builder()
                .a(snapshot.getName())
                .d(snapshot.getDescription())
                .d(snapshot.getLiterature());
        for (String keyword : snapshot.getKeywords()) {
            sv.bIdentifier(keyword);
        }
        return sv.build();
    }

    public SearchVector collectSampleSearchVector(SampleEntity sample) {
        CompoundEntity c = sample.getCompound();
        SearchVector.Builder sv = new SearchVector.Builder()
                .aIdentifier(sample.getSampleKey())
                .aIdentifier(sample.getNbkBatchNumber() != null ? sample.getNbkBatchNumber().toString() : null)
                .aIdentifier(c.getCasNumber())
                .b(c.getChemicalName());
        return sv.build();
    }

    public SearchVector collectNotebookSearchVector(NotebookSnapshot snapshot) {
        SearchVector.Builder sv = new  SearchVector.Builder()
                .aIdentifier(snapshot.getName())
                .d(snapshot.getDescription());
        return sv.build();
    }

    public Set<ExperimentSearchCompound> collectExperimentCompoundRefs(ExperimentSnapshot snapshot) {
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

    public List<String> collectExperimentRxnfiles(ExperimentSnapshot snapshot) {
        return StreamEx.of(snapshot.getModel().getReactions())
                .map(Reaction::getRxnfile)
                .nonNull()
                .toList();
    }

    public Set<ExperimentSearchBatch> collectExperimentBatches(ExperimentSnapshot snapshot) {
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
