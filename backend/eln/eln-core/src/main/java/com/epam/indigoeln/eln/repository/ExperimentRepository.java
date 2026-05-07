package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.util.Conditions;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class ExperimentRepository extends BaseRepository<ExperimentEntity> {

    private static final Sort SORT_SUGGEST = Sort.by("name");

    public ExperimentRepository() {
        super(EntityType.EXPERIMENT, ExperimentEntity.class);
    }

    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    ACLService aclService;

    public Page<ExperimentDTO> findAll(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable UserInfo createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .addIfNotNull("project.id=?", projectId)
                .addIfNotNull("notebook.id=?", notebookId)
                .addIfNotNull("createdBy.id = ?", createdByUser != null ? createdByUser.getId() : null);
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

    @Nullable
    public ExperimentEditSessionEntity findActiveEditSession(ExperimentEntity experiment, UserEntity user) {
        return em.createQuery("from ExperimentEditSession where experiment=:experiment and user=:user and finished is null", ExperimentEditSessionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("user", user)
                .getSingleResultOrNull();
    }

    public List<ExperimentRevisionEntity> findRecentRevisions(ExperimentEntity experiment, Duration period) {
        return em.createQuery("from ExperimentRevision where experiment=:experiment and datetime>=:since order by revision", ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment)
                .setParameter("since", ZonedDateTime.now().minusSeconds(period.toSeconds()))
                .getResultList();
    }

    public void closeInactiveEditSessions(ExperimentEntity experiment, Duration inactivityThreshold) {
        ZonedDateTime cutoff = ZonedDateTime.now().minus(inactivityThreshold);
        int updated = em.createQuery("""
                        update ExperimentEditSession
                        set finished=lastActive
                        where experiment=:experiment
                            and finished is null
                            and lastActive<:cutoff
                """)
                .setParameter("experiment", experiment)
                .setParameter("cutoff", cutoff)
                .executeUpdate();
        if (updated > 0) {
            log.info("{} edit sessions closed for inactivity", updated);
        }
    }

    public List<ExperimentRevisionSummaryDTO> getRevisionsSummary(ExperimentEntity experiment) {
        Stream<Object[]> stream = em.createNativeQuery("""
                WITH t AS (
                    SELECT user_id, summary, NULL date_from, datetime date_to, NULL edit_session_id
                    FROM Experiment_Revision
                    WHERE experiment_id=:experiment_id AND edit_session_id IS NULL
                    UNION ALL (
                        SELECT user_id, 'Edited experiment', MIN(datetime), MAX(datetime), edit_session_id
                        FROM Experiment_Revision
                        WHERE experiment_id=:experiment_id AND edit_session_id IS NOT NULL
                        GROUP BY edit_session_id, user_id
                    )
                )
                SELECT u.id, u.username, u.display_name, t.summary, t.date_from, t.date_to, t.edit_session_id
                FROM t
                JOIN User_Account u on u.id = t.user_id
                ORDER BY t.date_to DESC, t.date_from DESC;
                """)
                .setParameter("experiment_id", experiment.getId())
                .getResultStream();
        return stream
                .map(r -> new ExperimentRevisionSummaryDTO(
                        (UUID) r[6],
                        new com.epam.indigoeln.common.model.UserRef((UUID) r[0], (String) r[1], (String) r[2]),
                        (String) r[3],
                        r[4] != null ? ((Instant) r[4]).atZone(ZoneId.systemDefault()) : null,
                        ((Instant) r[5]).atZone(ZoneId.systemDefault())
                ))
                .toList();
    }

    public List<ExperimentRevisionEntity> getRevisions(ExperimentEntity experiment, @Nullable UUID editSessionId, boolean reverseOrder) {
        String condition = editSessionId != null ? "and editSession.id=:editSessionId" : "";
        String order = reverseOrder ? "desc" : "";
        TypedQuery<ExperimentRevisionEntity> query = em.createQuery("from ExperimentRevision where experiment=:experiment " + condition + " order by revision " + order, ExperimentRevisionEntity.class)
                .setParameter("experiment", experiment);
        if (editSessionId != null) {
            query.setParameter("editSessionId", editSessionId);
        }
        return query.getResultList();
    }

    public void persistEditSession(ExperimentEditSessionEntity session) {
        em.persist(session);
    }
}
