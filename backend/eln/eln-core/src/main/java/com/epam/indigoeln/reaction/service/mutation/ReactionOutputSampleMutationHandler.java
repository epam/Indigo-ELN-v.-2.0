package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;

public non-sealed interface ReactionOutputSampleMutationHandler<T extends ReactionOutputSampleMutation> extends MutationHandler<T> {

    void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, T mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context);

    @Override
    default void initContext(com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        context.setAffectsModel(true);
    }
}
