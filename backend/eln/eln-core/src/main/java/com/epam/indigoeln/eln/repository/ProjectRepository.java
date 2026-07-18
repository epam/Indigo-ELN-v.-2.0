package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ACLService;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;
import static com.epam.indigoeln.eln.model.ApplicationPermission.VIEW_PROJECTS;

@ApplicationScoped
public class ProjectRepository extends BaseRepository<ProjectEntity> {

    @Inject
    ProjectMapper projectMapper;
    @Inject
    ACLService aclService;

    public ProjectRepository() {
        super(ELNEntityType.PROJECT, ProjectEntity.class);
    }

    public Page<ProjectDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .addIfNotNull("createdBy = ?", createdByUser);
        if (search != null) {
            conditions.add("(name ilike ?) or (full_text_search(searchVector, websearch_to_tsquery('english', ?)))", '%' + search + '%', search);
        }

        Page<ProjectEntity> page = doFindWithTotals(
                conditions,
                paging,
                panacheSort,
                em.getEntityGraph("Project.list")
        );

        return map(page, projectMapper::entityToDTO);
    }

    public ProjectEntity load(UUID id) {
        ProjectEntity project = doLoad(id, em.getEntityGraph("Project.details"));
        aclService.ensureAccess(project, VIEW_PROJECTS);
        return project;
    }

    public TotalCounts getTotalCounts() {
        TotalCountsEntity entity = em.createQuery("from TotalCounts", TotalCountsEntity.class).getSingleResult();
        return projectMapper.convertTotalCounts(entity);
    }

    public boolean existsByName(String name) {
        return doFindOne(new Conditions().add("name=?", name)) != null;
    }

    public void lockProject(ProjectEntity project) {
        em.lock(project, LockModeType.PESSIMISTIC_WRITE);
    }

    public void persistRevision(ProjectRevisionEntity revision) {
        em.persist(revision);
    }

    public ProjectRevisionEntity getRevision(ProjectEntity project, int revision) {
        return em.createQuery("from ProjectRevision where project = :project and revision = :revision", ProjectRevisionEntity.class)
                .setParameter("project", project)
                .setParameter("revision", revision)
                .getSingleResult();
    }

    public List<ProjectRevisionEntity> findRecentRevisions(ProjectEntity project, Duration period) {
        return em.createQuery("from ProjectRevision where project=:project and datetime>=:since order by revision", ProjectRevisionEntity.class)
                .setParameter("project", project)
                .setParameter("since", Instant.now().minus(period))
                .getResultList();
    }

    public List<ProjectRevisionEntity> getRevisions(ProjectEntity project) {
        return em.createQuery("from ProjectRevision where project=:project order by revision", ProjectRevisionEntity.class)
                .setParameter("project", project)
                .getResultList();
    }
}
