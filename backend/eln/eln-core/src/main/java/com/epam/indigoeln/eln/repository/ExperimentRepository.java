package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.util.Conditions;
import com.epam.indigoeln.eln.util.ListWithTotal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ExperimentRepository extends BaseRepository<ExperimentEntity> {

    public ExperimentRepository() {
        super(EntityType.EXPERIMENT);
    }

    @Inject
    ExperimentMapper experimentMapper;

    public ListWithTotal<ExperimentDTO> findAll(@Nullable UUID projectId, @Nullable UUID notebookId, Paging paging) {
        return doFindWithTotals(
                new Conditions()
                        .addIfNotNull("project.id=?", projectId)
                        .addIfNotNull("notebook.id=?", notebookId),
                paging,
                DEFAULT_SORT,
                em.getEntityGraph("Experiment.list"),
                experimentMapper::entityToDTO
        );
    }

    public ExperimentDetailsDTO load(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Experiment.details"),
                experimentMapper::entityToDetailsDTO
        );
    }

    public void markExperiment(UUID experimentId, UserEntity user, boolean mark) {
        em.createNativeQuery("SELECT mark_experiment(?1, ?2, ?3)")
                .setParameter(1, experimentId)
                .setParameter(2, user.getId())
                .setParameter(3, mark)
                .getSingleResult();
    }

    public List<ExperimentEntity> findByProjectWithACLEntities(ProjectEntity project) {
        return find("project", project)
                .withHint("jakarta.persistence.fetchgraph", em.getEntityGraph("Experiment.withACL"))
                .list();
    }

    public List<ExperimentEntity> findByNotebookWithACLEntities(NotebookEntity notebook) {
        return find("notebook", notebook)
                .withHint("jakarta.persistence.fetchgraph", em.getEntityGraph("Experiment.withACL"))
                .list();
    }

    public boolean hasAccessibleExperiments(NotebookEntity notebook) {
        return find("notebook", notebook).firstResult() != null;
    }
}
