package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
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
        super(EntityType.NOTEBOOK);
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

    public NotebookDetailsDTO loadDetails(UUID id) {
        NotebookEntity notebook = doLoadDetails(
                id,
                em.getEntityGraph("Notebook.details"),
                Function.identity()
        );
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        return notebookMapper.entityToDetailsDTO(notebook);
    }

    public List<NotebookEntity> findByProjectWithACLEntities(ProjectEntity project) {
        return find("project", project)
                .withHint("jakarta.persistence.loadgraph", em.getEntityGraph("Notebook.withACL"))
                .list();
    }

    public boolean hasAccessibleNotebooks(ProjectEntity project) {
        return find("project", project).firstResult() != null;
    }
}
