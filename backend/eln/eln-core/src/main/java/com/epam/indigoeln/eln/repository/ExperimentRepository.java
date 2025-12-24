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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class ExperimentRepository extends BaseRepository<ExperimentEntity> {

    public ExperimentRepository() {
        super(EntityType.EXPERIMENT);
    }

    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    ACLService aclService;

    public Page<ExperimentDTO> findAll(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable SortOrder sort, @Nullable UserInfo createdByUser, Paging paging, boolean showAll) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("modifiedAt");
            case LATEST -> Sort.descending("modifiedAt");
        };

        Conditions conditions = new Conditions()
                .addIf(!showAll, "calculatedInfo.currentAccess is not null")
                .addIfNotNull("project.id=?", projectId)
                .addIfNotNull("notebook.id=?", notebookId)
                .addIfNotNull("createdBy.id = ?", createdByUser != null ? createdByUser.getId() : null);

        return doFindWithTotals(
                conditions,
                paging,
                panacheSort,
                em.getEntityGraph("Experiment.list"),
                experimentMapper::entityToDTO
        );
    }

    public ExperimentDetailsDTO load(UUID id) {
        ExperimentEntity experiment = doLoadDetails(
                id,
                em.getEntityGraph("Experiment.details"),
                Function.identity()
        );
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experimentMapper.entityToDetailsDTO(experiment);
    }

    public ExperimentEntity loadForReport(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Experiment.forReport"),
                Function.identity()
        );
    }

    public void markExperiment(UUID experimentId, UserEntity user, boolean mark) {
        em.createNativeQuery("SELECT mark_experiment(?1, ?2, ?3)")
                .setParameter(1, experimentId)
                .setParameter(2, user.getId())
                .setParameter(3, mark)
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
}
