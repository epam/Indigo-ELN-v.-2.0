package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;

public abstract class AbstractReactionOutputSampleMutationHandler<T extends ReactionOutputSampleMutation> extends ExperimentEditMutationHandlerBase<T> {

    protected static final String CANNOT_MODIFY_REGISTERED_SAMPLE_COMPOUND = "Cannot modify compound for a sample already sent for registration";

    @Override
    public String doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionOutputSample sample = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), sample.getRow().getReaction(), sample.getRow(), sample, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, T mutation, ExperimentMutationContext context);
}
