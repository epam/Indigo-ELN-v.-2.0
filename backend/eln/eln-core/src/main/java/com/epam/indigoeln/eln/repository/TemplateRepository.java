package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@ApplicationScoped
public class TemplateRepository extends BaseRepository<TemplateEntity> {

    @Inject
    TemplateMapper templateMapper;

    public TemplateRepository() {
        super(ELNEntityType.TEMPLATE, TemplateEntity.class);
    }

    public Page<TemplateDTO> findAll(
            @Nullable String search, @Nullable SortOrder sort,
            @Nullable UserEntity createdByUser, Paging paging, boolean showAll
    ) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
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
