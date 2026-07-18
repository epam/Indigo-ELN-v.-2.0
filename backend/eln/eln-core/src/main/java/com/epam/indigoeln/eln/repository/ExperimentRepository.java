package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UserRef;
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
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@ApplicationScoped
public class ExperimentRepository extends BaseRepository<ExperimentEntity> {

    private static final Sort SORT_SUGGEST = Sort.by("name");

    public ExperimentRepository() {
        super(ELNEntityType.EXPERIMENT, ExperimentEntity.class);
    }

    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    ACLService aclService;
    @Inject
    UserService userService;

    public Page<ExperimentDTO> findAll(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable UserRef createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .addIfNotNull("project.id=?", projectId)
                .addIfNotNull("notebook.id=?", notebookId)
                .addIfNotNull("createdBy.id = ?", createdByUser != null ? userService.getUserInfo(createdByUser).getId() : null);
        if (search != null) {
            conditions.add("(name ilike ?) or full_text_search(searchVector, websearch_to_tsquery('english', ?))", '%' + search + '%', search);
        }

        return doFindWithTotals(
                conditions,
                paging,
                panacheSort,
                em.getEntityGraph("Experiment.list"),
                experimentMapper::entityToDTO
        );
    }

    public ExperimentEntity loadAndLock(UUID id) {
        ExperimentEntity entity = findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) {
            throw new EntityNotFoundException(ELNEntityType.EXPERIMENT, id);
        }
        return entity;
    }

    public ExperimentEntity load(UUID id) {
        ExperimentEntity experiment = doLoadDetails(
                id,
                em.getEntityGraph("Experiment.details"),
                Function.identity()
        );
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experiment;
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

    public List<ExperimentDTO> findMarked() {
        return em.createQuery("from Experiment e where e.calculatedInfo.marked order by name", ExperimentEntity.class)
                .getResultList().stream()
                .map(experimentMapper::entityToDTO)
                .toList();
    }

    public List<ExperimentEntity> findByProjectWithACLEntities(ProjectEntity project) {
        return find("project", project)
                .withHint("jakarta.persistence.loadgraph", em.getEntityGraph("Experiment.withACL"))
                .list();
    }

    public List<ExperimentEntity> findByNotebookWithACLEntities(NotebookEntity notebook) {
        return find("notebook", notebook)
                .withHint("jakarta.persistence.loadgraph", em.getEntityGraph("Experiment.withACL"))
                .list();
    }

    public boolean hasAccessibleExperiments(NotebookEntity notebook) {
        return find("notebook", notebook).firstResult() != null;
    }

    @Nullable
    public String getLastExperimentName(NotebookEntity notebook) {
        List<@Nullable String> found = em.createQuery("select max(e.name) from Experiment e where e.notebook.id = ?1", String.class)
                .setParameter(1, notebook.getId())
                .getResultList();
        return found.isEmpty() || found.getFirst() == null ? null : found.getFirst();
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
}
