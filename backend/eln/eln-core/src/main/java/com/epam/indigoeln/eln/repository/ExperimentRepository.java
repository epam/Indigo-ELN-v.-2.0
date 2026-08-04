package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.CriteriaConditions;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@Slf4j
@ApplicationScoped
public class ExperimentRepository extends BaseRepository<ExperimentEntity> {

    private static final Sort SORT_SUGGEST = Sort.by("name");

    @Inject
    CriteriaConditions.Factory criteriaConditionsFactory;

    public ExperimentRepository() {
        super(ELNEntityType.EXPERIMENT, ExperimentEntity.class);
    }

    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    public Page<ExperimentDTO> findAll(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<ExperimentEntity> root = from(ExperimentEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                if (!showAll) {
                    conditions.add(isNotNull(root.get(ExperimentEntity_.currentAccessOrNull)));
                }
                if (projectId != null) {
                    conditions.add(root.get(ExperimentEntity_.project).get(ProjectEntity_.id).equalTo(projectId));
                }
                if (notebookId != null) {
                    conditions.add(root.get(ExperimentEntity_.notebook).get(NotebookEntity_.id).equalTo(notebookId));
                }
                if (createdByUser != null) {
                    conditions.add(root.get(ExperimentEntity_.createdBy).equalTo(createdByUser));
                }
                conditions.fullTextSearch(root.get(ExperimentEntity_.searchVector), search, s -> List.of(
                        ilike(root.get(ExperimentEntity_.name), '%' + s + '%')
                ));
            });
            orderBy(switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
                case EARLIEST -> asc(root.get(ExperimentEntity_.modifiedAt));
                case LATEST -> desc(root.get(ExperimentEntity_.modifiedAt));
            });
        }};

        Page<ExperimentEntity> page = doFindWithTotals(
                criteria,
                paging,
                em.getEntityGraph("Experiment.list")
        );

        return map(page, experimentMapper::entityToDTO);
    }

    public void lock(UUID id) {
        // See ProjectRepository.lock for the reasoning
        List<?> locked = em.createNativeQuery("select id from Experiment where id = ?1 for no key update")
                .setParameter(1, id)
                .getResultList();
        if (locked.isEmpty()) {
            throw new EntityNotFoundException(entityType, id);
        }
    }

    public ExperimentEntity load(UUID id) {
        ExperimentEntity experiment = doLoad(id, em.getEntityGraph("Experiment.details"));
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experiment;
    }

    public ExperimentEntity loadAndLock(UUID id) {
        lock(id);
        return load(id);
    }

    public void markExperiment(UUID experimentId, UserEntity user, boolean mark) {
        em.createNativeQuery("SELECT mark_experiment(?1, ?2, ?3)")
                .setParameter(1, experimentId)
                .setParameter(2, user.getId())
                .setParameter(3, mark)
                .getSingleResult();
    }

    public ExperimentEntity findBySignatureNumber(String signatureNumber) {
        return (ExperimentEntity) em.createQuery("from Experiment where signatureNumber = :signatureNumber")
                .setParameter("signatureNumber", signatureNumber)
                .getSingleResult();
    }

    public List<ExperimentDTO> findMarked(boolean showAll) {
        TypedQuery<ExperimentEntity> query = !showAll
                ? em.createQuery("from Experiment e where e.markedOrNull and currentAccessOrNull is not null order by name", ExperimentEntity.class)
                : em.createQuery("from Experiment e where e.markedOrNull order by name", ExperimentEntity.class);
        return map(query.getResultList(), experimentMapper::entityToDTO);
    }

    public List<ExperimentEntity> findByProjectWithACLEntities(ProjectEntity project) {
        //noinspection unchecked
        List<UUID> ids = em.createNativeQuery("select id from Experiment where project_id = ?1 for no key update", UUID.class)
                .setParameter(1, project.getId())
                .getResultList();
        return doFindByIDs(ids, em.getEntityGraph("Experiment.withACL"));
    }

    public List<ExperimentEntity> findByNotebookWithACLEntities(NotebookEntity notebook) {
        //noinspection unchecked
        List<UUID> ids = em.createNativeQuery("select id from Experiment where notebook_id = ?1 for no key update", UUID.class)
                .setParameter(1, notebook.getId())
                .getResultList();
        return doFindByIDs(ids, em.getEntityGraph("Experiment.withACL"));
    }

    public boolean hasAccessibleExperiments(NotebookEntity notebook) {
        return doFindOne(new Conditions().add("notebook=?", notebook)) != null;
    }

    @Nullable
    public String getLastExperimentName(NotebookEntity notebook) {
        List<@Nullable String> found = em.createQuery("select max(e.name) from Experiment e where e.notebook.id = ?1", String.class)
                .setParameter(1, notebook.getId())
                .getResultList();
        return found.isEmpty() || found.getFirst() == null ? null : found.getFirst();
    }

    public UUID getProjectID(UUID experimentID) {
        return em.createQuery("select project.id from Experiment where id = ?1", UUID.class)
                .setParameter(1, experimentID)
                .getSingleResult();
    }

    public void persistRevision(ExperimentRevisionEntity revision) {
        em.persist(revision);
    }

    public ExperimentRevisionEntity getRevision(ExperimentEntity experiment, int revision) {
        return em.createQuery("from ExperimentRevision where experiment = :experiment and revision = :revision", ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("revision", revision)
                .getSingleResult();
    }

    public ExperimentRevisionEntity getVersion(ExperimentEntity experiment, int version) {
        return em.createQuery("from ExperimentRevision where experiment = :experiment and version = :version", ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("version", version)
                .getSingleResult();
    }

    public List<ExperimentRef> resolveRefs(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery("select new com.epam.indigoeln.eln.model.ExperimentRef(id, name) from Experiment where id in :ids", ExperimentRef.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    public LinkedExperimentRefs resolveLinkedExperimentRefs(ExperimentEntity experiment) {
        Set<UUID> ids = StreamEx.of(experiment.getLinkedExperiments())
                .append(experiment.getContinuedFrom())
                .append(experiment.getContinuedTo())
                .toSet();
        Map<UUID, String> names = StreamEx.of(resolveRefs(ids))
                .toMap(ExperimentRef::getId, ExperimentRef::getName);
        return new LinkedExperimentRefs(
                toRefs(experiment.getLinkedExperiments(), names),
                toRefs(experiment.getContinuedFrom(), names),
                toRefs(experiment.getContinuedTo(), names)
        );
    }

    private static List<ExperimentRef> toRefs(UUID[] ids, Map<UUID, String> names) {
        return StreamEx.of(ids)
                .mapToEntry(id -> id, names::get)
                .nonNullValues()
                .mapKeyValue(ExperimentRef::new)
                .toList();
    }

    public List<ExperimentRef> suggest(@Nullable String search) {
        String condition = search != null ? "where name like :search" : "";
        TypedQuery<ExperimentRef> query = em.createQuery("select new com.epam.indigoeln.eln.model.ExperimentRef(id, name) from Experiment " + condition + " order by name", ExperimentRef.class);
        if (search != null) {
            query.setParameter("search", search + '%');
        }
        return query.setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
    }

    public Integer getLastUsedVersion(ExperimentEntity experiment) {
        return em.createQuery("select max(version) from ExperimentRevision where experiment=:experiment", Integer.class)
                .setParameter("experiment", experiment)
                .getSingleResult();
    }

    public List<ExperimentRevisionEntity> findRecentRevisions(ExperimentEntity experiment, Duration period) {
        return em.createQuery("from ExperimentRevision where experiment=:experiment and datetime>=:since order by revision", ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("since", Instant.now().minus(period))
                .getResultList();
    }

    public List<ExperimentRevisionEntity> getRevisions(ExperimentEntity experiment, boolean reverseOrder) {
        String order = reverseOrder ? "desc" : "";
        return em.createQuery("from ExperimentRevision where experiment=:experiment order by revision " + order, ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setHint("jakarta.persistence.loadgraph", "ExperimentRevision.list")
                .getResultList();
    }

    public List<ExperimentRevisionEntity> getRevisionRange(ExperimentEntity experiment, int revisionFrom) {
        return em.createQuery("from ExperimentRevision where experiment=:experiment and revision>=:revisionFrom order by revision", ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("revisionFrom", revisionFrom)
                .setHint("jakarta.persistence.loadgraph", "ExperimentRevision.range")
                .getResultList();
    }

    public record LinkedExperimentRefs(
            List<ExperimentRef> linkedExperiments,
            List<ExperimentRef> continuedFrom,
            List<ExperimentRef> continuedTo
    ) {}
}
