package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.mapper.NotebookMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.NotebookDTO;
import com.epam.indigoeln.eln.model.NotebookDetailsDTO;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.util.Conditions;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public ListWithTotal<NotebookDTO> findAll(UUID projectId, @Nullable String search, Paging paging) {
        return doFindWithTotals(
                new Conditions()
                        .add("project.id=?", projectId)
                        .addIfNotNull("full_text_search(searchVector, to_tsquery('english', ?))", search),
                paging,
                DEFAULT_SORT,
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
                .withHint("jakarta.persistence.fetchgraph", em.getEntityGraph("Notebook.withACL"))
                .list();
    }

    public boolean hasAccessibleNotebooks(ProjectEntity project) {
        return find("project", project).firstResult() != null;
    }
}
