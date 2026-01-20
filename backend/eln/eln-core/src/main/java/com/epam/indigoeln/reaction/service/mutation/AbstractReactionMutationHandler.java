package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionMutationHandler<T extends ReactionMutation, R extends MutationRedoInfo> extends ExperimentMutationHandlerBase<T, R> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, T mutation, @Nullable R redoInfo) {
        Preconditions.checkArgument(model != null);
        Reaction reaction = model.locate(mutation.anchor());
        return handle(experiment, model, reaction, mutation, redoInfo);
    }

    @Override
    public boolean isAffectsModel() {
        return true;
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, T mutation, @Nullable R redoInfo);
}
