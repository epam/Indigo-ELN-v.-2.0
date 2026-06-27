package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;

public abstract class AbstractReactionInputMutationHandler<T extends ReactionInputMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public String doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ReactionInput row = experiment.getModel().locate(mutation.anchor());
        return handle(experiment, experiment.getModel(), row.getReaction(), row, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, ExperimentMutationContext context);
}
