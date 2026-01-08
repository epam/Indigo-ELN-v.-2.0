package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;

public non-sealed interface ExperimentMutationHandler<T extends ExperimentMutation> extends MutationHandler<T, MutationRedoInfo> {
}
