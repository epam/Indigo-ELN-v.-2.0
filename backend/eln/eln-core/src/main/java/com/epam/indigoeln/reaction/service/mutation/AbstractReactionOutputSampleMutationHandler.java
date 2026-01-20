package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionOutputSampleMutationHandler<T extends ReactionOutputSampleMutation, R extends MutationRedoInfo> extends ExperimentMutationHandlerBase<T, R> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, @Nullable ExperimentModel model, T mutation, @Nullable R redoInfo) {
        Preconditions.checkArgument(model != null);
        ReactionOutputSample sample = model.locate(mutation.anchor());
        return handle(experiment, model, sample.getRow().getReaction(), sample.getRow(), sample, mutation, redoInfo);
    }

    @Override
    public boolean isAffectsModel() {
        return true;
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, T mutation, @Nullable R redoInfo);
}
