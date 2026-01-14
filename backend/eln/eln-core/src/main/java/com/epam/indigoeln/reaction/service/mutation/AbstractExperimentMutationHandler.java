package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;

public abstract class AbstractExperimentMutationHandler<T extends Mutation> extends ExperimentMutationHandlerBase<T, MutationRedoInfo> {
}
