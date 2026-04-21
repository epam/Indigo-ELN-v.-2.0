package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractReactionInputMutationHandler<T extends ReactionInputMutation> extends ExperimentMutationHandlerBase<T> {

    @Override
    public void doPrepare(ExperimentEntity entity, T mutation, ExperimentMutationContext context) {
        context.setAffectsModel(true);
        context.setRequiresEditSession(true);
    }

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, T mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ExperimentModel model = checkNotNull(experiment.getModelObj());
        ReactionInput row = model.locate(mutation.anchor());
        return doHandle(experiment, model, row.getReaction(), row, mutation, context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected abstract MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, ExperimentMutationContext context);
}
