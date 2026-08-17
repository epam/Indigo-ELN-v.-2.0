package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;

public interface ExperimentMutationListener extends MutationListener<ExperimentEntity, ExperimentMutationContext> {

    default void beforeRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
    }

    default void afterRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
    }
}
