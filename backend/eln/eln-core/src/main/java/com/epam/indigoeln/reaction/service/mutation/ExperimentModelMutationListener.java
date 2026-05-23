package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;

public abstract class ExperimentModelMutationListener {

    public void beforeHandle(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
    }

    public void afterRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
    }
}
