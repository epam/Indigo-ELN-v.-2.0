package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;

public abstract class AbstractReactionInputMutationHandler<T extends ReactionInputMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public void doPrepare(ExperimentEntity entity, T mutation, ExperimentMutationContext context) {
        context.setRequiresEditSession(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionInput row = experiment.getModel().locate(mutation.anchor());
        return doHandle(experiment, experiment.getModel(), row.getReaction(), row, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, ExperimentMutationContext context);
}
