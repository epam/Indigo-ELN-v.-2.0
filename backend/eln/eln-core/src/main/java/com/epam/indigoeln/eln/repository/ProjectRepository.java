package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.util.Conditions;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;

import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class ProjectRepository extends BaseRepository<ProjectEntity> {

    @Inject
    ProjectMapper projectMapper;
    @Inject
    ACLService aclService;

    public ProjectRepository() {
        super(EntityType.PROJECT);
    }

    public Page<ProjectDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .addIfNotNull("full_text_search(searchVector, websearch_to_tsquery('english', ?))", search)
                .addIfNotNull("createdBy = ?", createdByUser);

        return doFindWithTotals(
                conditions,
                paging,
                panacheSort,
                em.getEntityGraph("Project.list"),
                projectMapper::entityToDTO
        );
    }

    public ProjectDetailsDTO loadDetails(UUID id) {
        ProjectEntity project = doLoadDetails(
                id,
                em.getEntityGraph("Project.details"),
                Function.identity()
        );
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        return projectMapper.entityToDetailsDTO(project);
    }

    public TotalCounts getTotalCounts() {
        TotalCountsEntity entity = em.createQuery("from TotalCounts", TotalCountsEntity.class).getSingleResult();
        return projectMapper.convertTotalCounts(entity);
    }

    public void lockProject(ProjectEntity project) {
        em.lock(project, LockModeType.PESSIMISTIC_WRITE);
    }
}
