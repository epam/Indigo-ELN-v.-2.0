package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;

public abstract class AbstractExperimentMutationHandler<T extends ExperimentMutation> extends AbstractMutationHandler<T, MutationRedoInfo> {
}
