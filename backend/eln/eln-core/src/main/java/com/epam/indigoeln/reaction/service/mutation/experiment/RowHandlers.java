package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowEQ.class)
class SetInputRowEQHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowEQ, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowEQ mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        Double oldValue = row.getEq().getValue();
        row.setEq(EnteredValue.userLastEntered(MoreObjects.firstNonNull(mutation.eq(), 1.0), NoUnit.NO_UNIT));
        return new MutationResult(mutationHelper.formatSetterSummary("input EQ", mutation.eq())
                , null
                , new ReactionInputMutation.SetInputRowEQ(mutation.anchor(), oldValue)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowEQ.class)
class SetOutputRowEQHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowEQ, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowEQ mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        Double oldValue = row.getEq().getValue();
        row.setEq(EnteredValue.userLastEntered(MoreObjects.firstNonNull(mutation.eq(), 1.0), NoUnit.NO_UNIT));
        return new MutationResult(mutationHelper.formatSetterSummary("output EQ", mutation.eq())
                , null
                , new ReactionOutputMutation.SetOutputRowEQ(mutation.anchor(), oldValue)
        );
    }
}
