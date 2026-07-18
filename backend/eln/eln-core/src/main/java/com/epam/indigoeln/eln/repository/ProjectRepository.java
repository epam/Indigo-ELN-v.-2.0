package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.entity.ProjectEntity_.CalculatedInfo_;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ACLService;
import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<ProjectEntity> root = from(ProjectEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            List<Predicate> conditions = new ArrayList<>();
            if (!showAll) {
                conditions.add(isNotNull(root.get(ProjectEntity_.calculatedInfo).get(CalculatedInfo_.currentAccess)));
            }
            if (createdByUser != null) {
                conditions.add(root.get(ProjectEntity_.createdBy).equalTo(createdByUser));
            }
            if (search != null) {
                conditions.add(or(
                        ilike(root.get(ProjectEntity_.name), '%' + search + '%'),
                        isTrue(function("full_text_search", Boolean.class, root.get(ProjectEntity_.searchVector), literal("english"), literal(search)))
                ));
            }
            where(conditions);
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
