package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.SignatureTemplateEntity;
import com.epam.indigoeln.eln.mapper.SignatureTemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class SignatureTemplateRepository extends BaseRepository<SignatureTemplateEntity> {

    @Inject
    SignatureTemplateMapper signatureTemplateMapper;

    public SignatureTemplateRepository() {
        super(EntityType.SIGNATURE_TEMPLATE);
    }

    public Page<SignatureTemplateDTO> findAll(Paging paging) {
        return doFindWithTotals(
                Conditions.EMPTY,
                paging,
                DEFAULT_SORT,
                em.getEntityGraph("SignatureTemplate.list"),
                signatureTemplateMapper::entityToDTO
        );
    }

    public SignatureTemplateDetailsDTO loadDetails(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("SignatureTemplate.details"),
                signatureTemplateMapper::entityToDetailsDTO
        );
    }
}
