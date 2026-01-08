package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import org.jspecify.annotations.Nullable;

public non-sealed interface ReactionOutputSampleMutationHandler<T extends ReactionOutputSampleMutation, R extends MutationRedoInfo> extends MutationHandler<T, R> {

    @Override
    default MutationResult handle(ExperimentEntity experiment, T mutation, @Nullable R redoInfo, MutationContext context) {
        ReactionOutputSample sample = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), sample.getRow().getReaction(), sample.getRow(), sample, mutation, redoInfo, context);
    }

    MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, T mutation, @Nullable R redoInfo, MutationContext context);

    @Override
    default void initContext(ExperimentEntity experiment, T mutation, MutationContext context) {
        context.setAffectsModel(true);
    }
}
