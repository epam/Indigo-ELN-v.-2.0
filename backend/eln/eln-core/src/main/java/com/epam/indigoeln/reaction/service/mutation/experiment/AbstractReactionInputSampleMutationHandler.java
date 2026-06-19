package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;

public abstract class AbstractReactionInputSampleMutationHandler<T extends ReactionInputSampleMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public String doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionInputSample sample = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), sample.getRow().getReaction(), sample.getRow(), sample, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, T mutation, ExperimentMutationContext context);
}
