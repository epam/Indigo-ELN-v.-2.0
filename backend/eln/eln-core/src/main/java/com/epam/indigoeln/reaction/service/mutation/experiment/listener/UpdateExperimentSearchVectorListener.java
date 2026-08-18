package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.eln.util.SearchVectorField;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateExperimentSearchVectorListener implements ExperimentMutationListener {

    @Inject
    ExperimentService experimentService;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private List<@Nullable SearchVectorField> oldFields;

    @Override
    public void beforeHandle(ExperimentEntity experiment, ExperimentMutationContext context) {
        oldFields = experimentService.collectSearchFields(experiment);
    }

    @Override
    public void afterUpdateEntity(ExperimentEntity experiment) {
        List<@Nullable SearchVectorField> newFields = experimentService.collectSearchFields(experiment);
        if (!newFields.equals(oldFields)) {
            experimentService.updateSearchVector(experiment, newFields);
        }
    }
}
