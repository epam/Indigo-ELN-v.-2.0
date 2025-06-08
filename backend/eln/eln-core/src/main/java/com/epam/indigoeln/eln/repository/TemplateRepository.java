package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.util.Conditions;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class TemplateRepository extends BaseRepository<TemplateEntity> {

    @Inject
    TemplateMapper templateMapper;

    public TemplateRepository() {
        super(EntityType.TEMPLATE);
    }

    public ListWithTotal<TemplateDTO> findAll(Paging paging) {
        return doFindWithTotals(
                Conditions.EMPTY,
                paging,
                DEFAULT_SORT,
                em.getEntityGraph("Template.list"),
                templateMapper::entityToDTO
        );
    }

    public TemplateDetailsDTO loadDetails(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Template.details"),
                templateMapper::entityToDetailsDTO
        );
    }
}
