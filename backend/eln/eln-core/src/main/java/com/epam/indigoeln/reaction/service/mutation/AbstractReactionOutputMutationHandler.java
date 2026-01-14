package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionOutputMutationHandler<T extends ReactionOutputMutation, R extends MutationRedoInfo> extends ExperimentMutationHandlerBase<T, R> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, @Nullable ExperimentModel model, T mutation, @Nullable R redoInfo, MutationContext context) {
        Preconditions.checkArgument(model != null);
        ReactionOutput row = model.locate(mutation.anchor());
        return handle(experiment, model, row.getReaction(), row, mutation, redoInfo, context);
    }

    @Override
    protected boolean isAffectsModel() {
        return true;
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, T mutation, @Nullable R redoInfo, MutationContext context);
}
