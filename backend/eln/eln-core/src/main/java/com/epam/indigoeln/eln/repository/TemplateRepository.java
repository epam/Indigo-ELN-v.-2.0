package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.epam.indigoeln.eln.model.SortOrder.EARLIEST;
import static com.epam.indigoeln.eln.model.SortOrder.LATEST;

@ApplicationScoped
public class TemplateRepository extends BaseRepository<TemplateEntity> {

    @Inject
    TemplateMapper templateMapper;

    public TemplateRepository() {
        super(EntityType.TEMPLATE, TemplateEntity.class);
    }

    public Page<TemplateDTO> findAll(
            @Nullable String search, @Nullable SortOrder sort,
           @Nullable UserEntity createdByUser, Paging paging, boolean showAll
    ) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIfNotNull("lower(name) LIKE ?", search != null ? "%" + search.toLowerCase() + "%" : null)
                .addIfNotNull("createdBy = ?", createdByUser);

        return doFindWithTotals(
                conditions,
                paging,
                panacheSort,
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
