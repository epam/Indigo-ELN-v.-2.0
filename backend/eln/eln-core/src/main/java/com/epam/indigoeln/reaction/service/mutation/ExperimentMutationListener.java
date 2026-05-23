package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;

public abstract class ExperimentMutationListener<T extends ExperimentMutation> {

    public void beforeHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context) {
    }

    public void afterHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context) {
    }
}
