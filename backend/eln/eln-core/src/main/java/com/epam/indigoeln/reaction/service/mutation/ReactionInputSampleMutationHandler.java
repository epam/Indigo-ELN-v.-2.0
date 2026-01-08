package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import org.jspecify.annotations.Nullable;

public non-sealed interface ReactionInputSampleMutationHandler<T extends ReactionInputSampleMutation, R extends MutationRedoInfo> extends MutationHandler<T, R> {

    @Override
    default MutationResult handle(ExperimentEntity experiment, T mutation, @Nullable R redoInfo, MutationContext context) {
        ReactionInputSample sample = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), sample.getRow().getReaction(), sample.getRow(), sample, mutation, redoInfo, context);
    }

    MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, T mutation, @Nullable R redoInfo, MutationContext context);

    @Override
    default void initContext(ExperimentEntity experiment, T mutation, MutationContext context) {
        context.setAffectsModel(true);
    }
}
