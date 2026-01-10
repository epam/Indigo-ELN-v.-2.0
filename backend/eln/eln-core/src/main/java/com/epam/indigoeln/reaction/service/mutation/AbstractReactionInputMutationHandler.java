package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import org.jspecify.annotations.Nullable;

public abstract class AbstractReactionInputMutationHandler<T extends ReactionInputMutation, R extends MutationRedoInfo> extends AbstractMutationHandler<T, R> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, T mutation, @Nullable R redoInfo, MutationContext context) {
        ReactionInput row = model.locate(mutation.anchor());
        return handle(experiment, model, row.getReaction(), row, mutation, redoInfo, context);
    }

    protected abstract MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, T mutation, @Nullable R redoInfo, MutationContext context);

    @Override
    public void initContext(ExperimentEntity experiment, T mutation, MutationContext context) {
        context.setAffectsModel(true);
    }
}
