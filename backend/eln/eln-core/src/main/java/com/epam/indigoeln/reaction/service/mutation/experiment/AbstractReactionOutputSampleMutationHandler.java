package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;

public abstract class AbstractReactionOutputSampleMutationHandler<T extends ReactionOutputSampleMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public void doPrepare(ExperimentEntity entity, T mutation, ExperimentMutationContext context) {
        context.setRequiresEditSession(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionOutputSample sample = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), sample.getRow().getReaction(), sample.getRow(), sample, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, T mutation, ExperimentMutationContext context);
}
