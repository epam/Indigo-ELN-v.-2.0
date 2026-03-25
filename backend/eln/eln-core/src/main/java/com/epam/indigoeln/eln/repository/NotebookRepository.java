package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.util.Conditions;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@ApplicationScoped
public class NotebookRepository extends BaseRepository<NotebookEntity> {

    @Inject
    NotebookMapper notebookMapper;
    @Inject
    ACLService aclService;

    public NotebookRepository() {
        super(EntityType.NOTEBOOK, NotebookEntity.class);
    }

    public Page<NotebookDTO> findAll(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .add("project.id=?", projectId)
                .addIfNotNull("full_text_search(searchVector, websearch_to_tsquery('english', ?))", search)
                .addIfNotNull("createdBy = ?", createdByUser);

        return doFindWithTotals(
                conditions,
                paging,
                panacheSort,
                em.getEntityGraph("Notebook.list"),
                notebookMapper::entityToDTO
        );
    }

    public NotebookEntity loadDetails(UUID id) {
        NotebookEntity notebook = doLoadDetails(
                id,
                em.getEntityGraph("Notebook.details"),
                Function.identity()
        );
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        return notebook;
    }

    public List<NotebookEntity> findByProjectWithACLEntities(ProjectEntity project) {
        return find("project", project)
                .withHint("jakarta.persistence.loadgraph", em.getEntityGraph("Notebook.withACL"))
                .list();
    }

    public boolean hasAccessibleNotebooks(ProjectEntity project) {
        return find("project", project).firstResult() != null;
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public List<NestedACLEntryDTO> findNestedAccess(UUID notebookId) {
        @SuppressWarnings("unchecked")
        Stream<Object[]> stream = em.createQuery("select e, a from Experiment e " +
                        "join e.aclEntities a " +
                        "join fetch a.user " +
                        "where e.notebook.id = :notebookId " +
                        "and a.level != :implicitView"
                )
                .setParameter("notebookId", notebookId)
                .setParameter("implicitView", AccessLevel.IMPLICIT_VIEW)
                .getResultStream();
        return stream
                .map(arr -> {
                    ExperimentEntity entity = (ExperimentEntity) arr[0];
                    ExperimentACLEntity entry = (ExperimentACLEntity) arr[1];
                    return new NestedACLEntryDTO(EntityType.EXPERIMENT, entity.getId(), entity.getName(), entry.getUser().getId(), entry.getUser().getDisplayName(), entry.getLevel());
                })
                .toList();
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
                .setParameter("since", ZonedDateTime.now().minusSeconds(period.toSeconds()))
                .getResultList();
    }
}
