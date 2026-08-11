package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.util.CriteriaConditions;
import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;
import static com.epam.indigoeln.eln.model.ApplicationPermission.VIEW_PROJECTS;

@ApplicationScoped
public class ProjectRepository extends BaseRepository<ProjectEntity> {

    private static final int SUGGEST_LIMIT = 20;

    private static final String PROJECT = "project";

    @Inject
    ProjectMapper projectMapper;
    @Inject
    ACLService aclService;

    @Inject
    CriteriaConditions.Factory criteriaConditionsFactory;

    public ProjectRepository() {
        super(ELNEntityType.PROJECT, ProjectEntity.class);
    }

    public Page<ProjectDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<ProjectEntity> root = from(ProjectEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                if (!showAll) {
                    conditions.add(isNotNull(root.get(ProjectEntity_.currentAccessOrNull)));
                }
                if (createdByUser != null) {
                    conditions.add(root.get(ProjectEntity_.createdBy).equalTo(createdByUser));
                }
                conditions.fullTextSearch(root.get(ProjectEntity_.searchVector), search, s -> List.of(
                        ilike(root.get(ProjectEntity_.name), '%' + s + '%')
                ));
            });
            orderBy(switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
                case EARLIEST -> asc(root.get(ProjectEntity_.modifiedAt));
                case LATEST -> desc(root.get(ProjectEntity_.modifiedAt));
            });
        }};

        Page<ProjectEntity> page = doFindWithTotals(
                criteria,
                paging,
                em.getEntityGraph("Project.list")
        );

        return map(page, projectMapper::entityToDTO);
    }

    public void lock(UUID id) {
        // Lock the base table row directly via native SQL, entirely bypassing Hibernate's own lock tracking.
        // Project_Access_View (backing currentAccessOrNull) is a non-simple view (UNION ALL of LATERAL joins)
        // that Postgres cannot lock, and combining PESSIMISTIC_WRITE with it - eagerly or via a later fetch on
        // the locked entity - fails or leaves the entity stale (Hibernate reapplies a managed entity's lock
        // mode to later fetches, and re-find()ing an already-managed entity doesn't refresh its data). A plain
        // native row lock sidesteps all of that: once acquired, any prior holder has committed, so the ordinary
        // unlocked load below (via Project.details, which already includes currentAccessOrNull) reads current data.
        List<?> locked = em.createNativeQuery("select id from Project where id = ?1 for no key update")
                .setParameter(1, id)
                .getResultList();
        if (locked.isEmpty()) {
            throw new EntityNotFoundException(entityType, id);
        }
    }

    public ProjectEntity load(UUID id) {
        ProjectEntity project = doLoad(id, em.getEntityGraph("Project.details"));
        aclService.ensureAccess(project, VIEW_PROJECTS);
        return project;
    }

    public ProjectEntity loadAndLock(UUID id) {
        lock(id);
        return load(id);
    }

    public ProjectEntity loadWithACL(UUID id) {
        return doLoad(id, em.getEntityGraph("Project.withACL"));
    }

    public TotalCounts getTotalCounts() {
        TotalCountsEntity entity = em.createQuery("from TotalCounts", TotalCountsEntity.class).getSingleResult();
        return projectMapper.convertTotalCounts(entity);
    }

    public boolean existsByName(String name) {
        CriteriaDefinition<Integer> criteria = new CriteriaDefinition<>(em, Integer.class) {{
            JpaRoot<ProjectEntity> root = from(ProjectEntity.class);
            select(literal(1));
            where(root.get(ProjectEntity_.name).equalTo(name));
        }};
        return doExists(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<String> suggestKeywords(@Nullable String search) {
        String prefix = (search == null || search.isBlank()) ? "%" : search.toLowerCase() + "%";
        return em.createNativeQuery(
                        "select distinct keyword from project_keyword where lower(keyword) like ?1 order by keyword",
                        String.class)
                .setParameter(1, prefix)
                .setMaxResults(SUGGEST_LIMIT)
                .getResultList();
    }

    public void lockProject(ProjectEntity project) {
        em.lock(project, LockModeType.PESSIMISTIC_WRITE);
    }

    public void persistRevision(ProjectRevisionEntity revision) {
        em.persist(revision);
    }

    public ProjectRevisionEntity getRevision(ProjectEntity project, int revision) {
        return em.createQuery("from ProjectRevision where project = :project and revision = :revision", ProjectRevisionEntity.class)
                .setParameter(PROJECT, project)
                .setParameter("revision", revision)
                .getSingleResult();
    }

    public List<ProjectRevisionEntity> findRecentRevisions(ProjectEntity project, Duration period) {
        return em.createQuery("from ProjectRevision where project=:project and datetime>=:since order by revision", ProjectRevisionEntity.class)
                .setParameter(PROJECT, project)
                .setParameter("since", Instant.now().minus(period))
                .getResultList();
    }

    public List<ProjectRevisionEntity> getRevisions(ProjectEntity project) {
        return em.createQuery("from ProjectRevision where project=:project order by revision", ProjectRevisionEntity.class)
                .setParameter(PROJECT, project)
                .getResultList();
    }
}
