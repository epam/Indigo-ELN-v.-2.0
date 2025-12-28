package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.Mutation;

public sealed interface MutationHandler<T extends Mutation> permits
        ReactionMutationHandler,
        ReactionInputMutationHandler,
        ReactionInputSampleMutationHandler,
        ReactionOutputMutationHandler,
        ReactionOutputSampleMutationHandler,
        ExperimentMutationHandler
{
}
