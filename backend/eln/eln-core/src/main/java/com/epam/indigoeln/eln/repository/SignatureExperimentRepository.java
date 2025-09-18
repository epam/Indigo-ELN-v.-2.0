package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.mapper.SignatureExperimentMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public class SignatureExperimentRepository extends BaseRepository<ExperimentEntity> {

    @Inject
    SignatureExperimentMapper signatureExperimentMapper;
    @PersistenceContext
    EntityManager em;

    public SignatureExperimentRepository() {
        super(EntityType.EXPERIMENT);
    }

    public Page<ExperimentForSignatureDTO> findAll(Paging paging) {
        // first, load without entity graph to get proper paging
        Page<ExperimentEntity> page = doFindWithTotals(
                new Conditions()
                        .add("status in ?", List.of(ExperimentStatus.SUBMITTED, ExperimentStatus.SIGNING))
                        .add("id in (select experiment.id from ExperimentSignature where user.id=? and status is null)", userService.getCurrentUser().getId()),
                paging,
                DEFAULT_SORT,
                null,
                Function.identity()
        );
        // second, load entity graph
        em.createQuery("from Experiment e where e in :entities")
                .setParameter("entities", page.getItems())
                .setHint("jakarta.persistence.loadgraph", em.getEntityGraph("Experiment.forSignature"))
                .getResultList();
        return Page.of(page.getPaging(), page.getTotalItems(), signatureExperimentMapper.entityToDTOList(page.getItems()));
    }
}
