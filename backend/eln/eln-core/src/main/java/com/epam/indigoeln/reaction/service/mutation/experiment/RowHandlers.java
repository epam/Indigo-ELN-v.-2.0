package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.mutation.MutationContext;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionInputMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputMutationHandler;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowEQ.class)
class SetInputRowEQHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowEQ> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowEQ mutation, MutationContext context) {
        row.setEq(EnteredValue.userLastEntered(MoreObjects.firstNonNull(mutation.eq(), 1.0), NoUnit.NO_UNIT));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowEQ.class)
class SetOutputRowEQHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowEQ> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowEQ mutation, MutationContext context) {
        row.setEq(EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT, 1.0));
    }
}
