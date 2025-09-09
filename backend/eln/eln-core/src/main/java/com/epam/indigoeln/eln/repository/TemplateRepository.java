package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
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

    /**
     * Checks if a template with the given name exists.
     *
     * @param name the name of the template.
     * @return true if a template with the given name exists, false otherwise.
     */
    public boolean existsByName(String name) {
        return find("name", name).firstResultOptional().isPresent();
    }
}
