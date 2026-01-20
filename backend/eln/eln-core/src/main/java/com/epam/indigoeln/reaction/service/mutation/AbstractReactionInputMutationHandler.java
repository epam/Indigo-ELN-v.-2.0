package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionInputMutationHandler<T extends ReactionInputMutation, R extends MutationRedoInfo> extends ExperimentMutationHandlerBase<T, R> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, T mutation, @Nullable R redoInfo) {
        Preconditions.checkArgument(model != null);
        ReactionInput row = model.locate(mutation.anchor());
        return doHandle(experiment, model, row.getReaction(), row, mutation, redoInfo);
    }

    @Override
    public boolean isAffectsModel() {
        return true;
    }

    protected abstract MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, @Nullable R redoInfo);
}
