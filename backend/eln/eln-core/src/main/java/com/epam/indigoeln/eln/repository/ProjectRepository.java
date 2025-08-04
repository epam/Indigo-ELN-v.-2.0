package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.util.SortOrder;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import com.epam.indigoeln.eln.util.ListWithTotal;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ProjectRepository extends BaseRepository<ProjectEntity> {

    @Inject
    ProjectMapper projectMapper;

    public ProjectRepository() {
        super(EntityType.PROJECT);
    }

    public ListWithTotal<ProjectDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging) {
        Sort sortOrder = switch (sort) {
            case EARLIEST -> Sort.ascending("createdAt");
            case LATEST -> Sort.descending("createdAt");
            default -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIfNotNull("full_text_search(searchVector, to_tsquery('english', ?))", search)
                .addIfNotNull("createdBy = ?", createdByUser);

        return doFindWithTotals(
                conditions,
                paging,
                sortOrder,
                em.getEntityGraph("Project.list"),
                projectMapper::entityToDTO
        );
    }

    public ProjectDetailsDTO loadDetails(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Project.details"),
                projectMapper::entityToDetailsDTO
        );
    }

    public TotalCounts getTotalCounts() {
        TotalCountsEntity entity = em.createQuery("from TotalCounts", TotalCountsEntity.class).getSingleResult();
        return projectMapper.convertTotalCounts(entity);
    }
}
