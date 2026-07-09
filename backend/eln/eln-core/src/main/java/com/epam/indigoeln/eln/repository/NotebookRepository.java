package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.NotebookDTO;
import com.epam.indigoeln.eln.service.ACLService;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class NotebookRepository extends BaseRepository<NotebookEntity> {

    @Inject
    NotebookMapper notebookMapper;
    @Inject
    ACLService aclService;

    public NotebookRepository() {
        super(ELNEntityType.NOTEBOOK, NotebookEntity.class);
    }

    public Page<NotebookDTO> findAll(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @Nullable UserEntity createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .add("project.id=?", projectId)
                .addIfNotNull("createdBy = ?", createdByUser);
        if (search != null) {
            conditions.add("(name ilike ?) or full_text_search(searchVector, websearch_to_tsquery('english', ?))", '%' + search + '%', search);
        }

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
