package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.util.SearchVector;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.ProjectMutationListener;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateProjectSearchListener implements ProjectMutationListener {

    @Inject
    ProjectService projectService;
    @PersistenceContext
    EntityManager em;

    @Override
    public void beforePersist(ProjectEntity project, ProjectSnapshot snapshotBefore, ProjectSnapshot snapshotAfter) {
        // TODO detect from diff what parts potentially changed
        SearchVector oldSearchVector = projectService.collectSearchVector(snapshotBefore);
        SearchVector newSearchVector = projectService.collectSearchVector(snapshotAfter);
        boolean searchVectorChanged = !newSearchVector.equals(oldSearchVector);
        if (searchVectorChanged) {
            project.setSearchVector(newSearchVector);
        }
    }
}
