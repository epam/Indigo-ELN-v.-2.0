package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.util.SearchVectorField;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.ProjectMutationListener;
import com.epam.indigoeln.reaction.service.mutation.project.ProjectMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateProjectSearchVectorListener implements ProjectMutationListener {

    @Inject
    ProjectService projectService;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private List<@Nullable SearchVectorField> oldFields;

    @Override
    public void beforeHandle(ProjectEntity project, ProjectMutationContext context) {
        oldFields = projectService.collectSearchFields(project);
    }

    @Override
    public void afterUpdateEntity(ProjectEntity project) {
        List<@Nullable SearchVectorField> newFields = projectService.collectSearchFields(project);
        if (!newFields.equals(oldFields)) {
            projectService.updateSearchVector(project, newFields);
        }
    }
}
