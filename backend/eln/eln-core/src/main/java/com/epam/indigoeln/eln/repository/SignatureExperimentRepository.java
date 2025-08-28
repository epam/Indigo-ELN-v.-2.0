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
        return doFindWithTotals(
                new Conditions()
                        .add("status in ?", List.of(ExperimentStatus.SUBMITTED, ExperimentStatus.SIGNING))
                        .add("id in (select experiment.id from ExperimentSignature where user.id=? and status is null)", userService.getCurrentUser().getId()),
                paging,
                DEFAULT_SORT,
                em.getEntityGraph("Experiment.forSignature"),
                signatureExperimentMapper::entityToDTO
        );
    }
}
