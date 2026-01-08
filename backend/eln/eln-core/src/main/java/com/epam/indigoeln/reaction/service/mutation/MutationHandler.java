package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import org.jspecify.annotations.Nullable;

public sealed interface MutationHandler<T extends Mutation, R extends MutationRedoInfo> permits
        ReactionMutationHandler,
        ReactionInputMutationHandler,
        ReactionInputSampleMutationHandler,
        ReactionOutputMutationHandler,
        ReactionOutputSampleMutationHandler,
        ExperimentMutationHandler
{

    MutationResult handle(ExperimentEntity experiment, T mutation, @Nullable R redoInfo, MutationContext context);

    void initContext(ExperimentEntity experiment, T mutation, MutationContext context);
}
