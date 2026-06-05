package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;

public interface ExperimentModelMutationListener {

    int DEFAULT_PRIORITY = 1000;
    int VALIDATION_PRIORITY = 100;

    default void beforeHandle(ExperimentEntity experiment, ExperimentMutationContext context) {
    }

    default void beforeRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
    }

    default void afterRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
    }
}
