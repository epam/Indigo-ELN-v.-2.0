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
        // !!! load last 1 hour, and then drop leftmost revisions, so all remaining undo/redo has their initial revision loaded
        return projectRepository.findRecentRevisions(entity, Duration.of(365, ChronoUnit.DAYS));
    }
}
