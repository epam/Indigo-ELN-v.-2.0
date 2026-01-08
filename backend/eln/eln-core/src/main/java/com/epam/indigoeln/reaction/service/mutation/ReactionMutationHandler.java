package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import org.jspecify.annotations.Nullable;

public non-sealed interface ReactionMutationHandler<T extends ReactionMutation, R extends MutationRedoInfo> extends MutationHandler<T, R> {

    @Override
    default MutationResult handle(ExperimentEntity experiment, T mutation, @Nullable R redoInfo, MutationContext context) {
        Reaction reaction = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), reaction, mutation, redoInfo, context);
    }

    MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, T mutation, @Nullable R redoInfo, MutationContext context);

    @Override
    default void initContext(ExperimentEntity experiment, T mutation, MutationContext context) {
        context.setAffectsModel(true);
    }
}
