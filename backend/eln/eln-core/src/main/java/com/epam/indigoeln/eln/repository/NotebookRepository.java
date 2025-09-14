package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotebookRepository extends BaseRepository<NotebookEntity> {

    @Inject
    NotebookMapper notebookMapper;

    public NotebookRepository() {
        super(EntityType.NOTEBOOK);
    }

    public Page<NotebookDTO> findAll(UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                              @Nullable UserEntity createdByUser, Paging paging) {
        Sort panacheSort = switch (sort) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .add("project.id=?", projectId)
                .addIfNotNull("full_text_search(searchVector, to_tsquery('english', ?))", search)
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
        return doLoadDetails(
                id,
                em.getEntityGraph("Notebook.details"),
                notebookMapper::entityToDetailsDTO
        );
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
