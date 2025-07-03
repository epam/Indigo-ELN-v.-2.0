package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectRepository extends BaseRepository<ProjectEntity> {

    @Inject
    ProjectMapper projectMapper;

    public ProjectRepository() {
        super(EntityType.PROJECT);
    }

    public ListWithTotal<ProjectDTO> findAll(@Nullable String search, Paging paging) {
        return doFindWithTotals(
                new Conditions()
                        .addIfNotNull("full_text_search(searchVector, to_tsquery('english', ?))", search),
                paging,
                DEFAULT_SORT,
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

    public List<String> suggestProjectKeywords(@Nullable String search) {
        Query query = em.createNativeQuery(
                "SELECT unnest\n" +
                "FROM (SELECT DISTINCT UNNEST(keywords) FROM Project) t\n" +
                "WHERE " + (search != null ? "LOWER(unnest) LIKE ?" : "1=1") + "\n" +
                "ORDER BY unnest", String.class)
                .setMaxResults(10);
        if (search != null) {
            query.setParameter(1, search.toLowerCase() + '%');
        }
        //noinspection unchecked
        return (List<String>) query.getResultList();
    }
}
