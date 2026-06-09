package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;

public abstract class AbstractReactionOutputMutationHandler<T extends ReactionOutputMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public void doPrepare(ExperimentEntity entity, T mutation, ExperimentMutationContext context) {
        context.setRequiresEditSession(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionOutput row = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), row.getReaction(), row, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, T mutation, ExperimentMutationContext context);
}
