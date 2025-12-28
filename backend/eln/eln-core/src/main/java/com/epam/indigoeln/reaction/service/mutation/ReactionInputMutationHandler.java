package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;

public non-sealed interface ReactionInputMutationHandler<T extends ReactionInputMutation> extends MutationHandler<T> {

    void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, MutationContext context);
}
