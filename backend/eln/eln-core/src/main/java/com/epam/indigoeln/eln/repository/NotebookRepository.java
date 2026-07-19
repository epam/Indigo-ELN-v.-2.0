package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.entity.NotebookEntity_.CalculatedInfo_;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.NotebookDTO;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.util.CriteriaConditions;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.ws.rs.QueryParam;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class NotebookRepository extends BaseRepository<NotebookEntity> {

    @Inject
    NotebookMapper notebookMapper;
    @Inject
    ACLService aclService;
    @Inject
    CriteriaConditions.Factory criteriaConditionsFactory;

    public NotebookRepository() {
        super(ELNEntityType.NOTEBOOK, NotebookEntity.class);
    }

    public Page<NotebookDTO> findAll(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<NotebookEntity> root = from(NotebookEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                if (!showAll) {
                    conditions.add(isNotNull(root.get(NotebookEntity_.calculatedInfo).get(CalculatedInfo_.currentAccess)));
                }
                conditions.add(root.get(NotebookEntity_.project).get(ProjectEntity_.id).equalTo(projectId));
                if (createdByUser != null) {
                    conditions.add(root.get(NotebookEntity_.createdBy).equalTo(createdByUser));
                }
                conditions.fullTextSearch(root.get(NotebookEntity_.searchVector), search, s -> List.of(
                        ilike(root.get(NotebookEntity_.name), '%' + s + '%')
                ));
            });
            orderBy(switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
                case EARLIEST -> asc(root.get(NotebookEntity_.modifiedAt));
                case LATEST -> desc(root.get(NotebookEntity_.modifiedAt));
            });
        }};

        Page<NotebookEntity> page = doFindWithTotals(
                criteria,
                paging,
                em.getEntityGraph("Notebook.list")
        );

        return map(page, notebookMapper::entityToDTO);
    }

    public NotebookEntity loadDetails(UUID id) {
        NotebookEntity notebook = doLoad(id, em.getEntityGraph("Notebook.details"));
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        return notebook;
    }

    public List<NotebookEntity> findByProjectWithACLEntities(ProjectEntity project) {
        return doFind(new Conditions().add("project=?", project), null, null, em.getEntityGraph("Notebook.withACL"));
    }

    public boolean hasAccessibleNotebooks(ProjectEntity project) {
        return doFindOne(new Conditions().add("project=?", project)) != null;
    }

    public boolean existsByName(String name) {
        return doFindOne(new Conditions().add("name=?", name)) != null;
    }

    public void persistRevision(NotebookRevisionEntity revision) {
        em.persist(revision);
    }

    public NotebookRevisionEntity getRevision(NotebookEntity notebook, int revision) {
        return em.createQuery("from NotebookRevision where notebook = :notebook and revision = :revision", NotebookRevisionEntity.class)
                .setParameter("notebook", notebook)
                .setParameter("revision", revision)
                .getSingleResult();
    }

    public List<NotebookRevisionEntity> findRecentRevisions(NotebookEntity notebook, Duration period) {
        return em.createQuery("from NotebookRevision where notebook=:notebook and datetime>=:since order by revision", NotebookRevisionEntity.class)
                .setParameter("notebook", notebook)
                .setParameter("since", Instant.now().minus(period))
                .getResultList();
    }

    public List<NotebookRevisionEntity> getRevisions(NotebookEntity notebook) {
        return em.createQuery("from NotebookRevision where notebook=:notebook order by revision", NotebookRevisionEntity.class)
                .setParameter("notebook", notebook)
                .getResultList();
    }
}
