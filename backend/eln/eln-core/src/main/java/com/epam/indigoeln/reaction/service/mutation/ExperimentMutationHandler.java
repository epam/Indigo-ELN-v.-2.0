package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;

public non-sealed interface ExperimentMutationHandler<T extends ExperimentMutation> extends MutationHandler<T> {

    MutationResult handle(ExperimentEntity experiment, T mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context);
}
