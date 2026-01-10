package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionMutationHandler<T extends ReactionMutation, R extends MutationRedoInfo> extends AbstractMutationHandler<T, R> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, T mutation, @Nullable R redoInfo, MutationContext context) {
        Reaction reaction = model.locate(mutation.anchor());
        return handle(experiment, model, reaction, mutation, redoInfo, context);
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, T mutation, @Nullable R redoInfo, MutationContext context);

    @Override
    public void initContext(ExperimentEntity experiment, T mutation, MutationContext context) {
        context.setAffectsModel(true);
    }
}
