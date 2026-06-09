package com.epam.indigoeln.reaction.service.mutation.project;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.service.AbstractUndoHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class ProjectUndoHelper extends AbstractUndoHelper<ProjectEntity, ProjectSnapshot, ProjectRevisionEntity, ProjectMutationContext> {

    @Inject
    ProjectRepository projectRepository;

    ProjectUndoHelper() {
        super(ProjectSnapshot.class);
    }

    @Override
    protected List<ProjectRevisionEntity> loadRevisions(ProjectEntity entity) {
        // TODO load last 1 hour, and either deny undoing beyond that, or load previous data if needed for redo
        return projectRepository.findRecentRevisions(entity, Duration.of(365, ChronoUnit.DAYS));
    }
}
