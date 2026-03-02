package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.UUID;

@ApplicationScoped
public class TemplateRepository extends BaseRepository<TemplateEntity> {

    @Inject
    TemplateMapper templateMapper;

    public TemplateRepository() {
        super(EntityType.TEMPLATE, TemplateEntity.class);
    }

    public Page<TemplateDTO> findAll(Paging paging) {
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

    public TemplateDetailsDTO findByName(String name) {
        TemplateDetailsDTO template = doFindOne(
                new Conditions().add("lower(name) = ?", name.toLowerCase()),
                em.getEntityGraph("Template.details"),
                templateMapper::entityToDetailsDTO
        );
        if (template == null) {
            throw new NotFoundException("Template not found");
        }
        return template;
    }
}
