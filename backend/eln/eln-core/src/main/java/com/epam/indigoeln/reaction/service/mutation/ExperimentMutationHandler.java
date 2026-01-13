package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import org.jspecify.annotations.Nullable;

public interface ExperimentMutationHandler<T extends Mutation, R extends MutationRedoInfo> extends MutationHandler {

    MutationResult handle(ExperimentEntity experiment, ExperimentModel model, T mutation, @Nullable R redoInfo, MutationContext context);

    void initContext(ExperimentEntity experiment, T mutation, MutationContext context);
}
