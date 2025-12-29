package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;

public non-sealed interface ReactionMutationHandler<T extends ReactionMutation> extends MutationHandler<T> {

    void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, T mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context);

    @Override
    default void initContext(com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        context.setAffectsModel(true);
    }
}
