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
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowEQ.class)
class SetInputRowEQHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowEQ> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowEQ mutation) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(row::getEq, row::setEq, mutation.eq(), NoUnit.NO_UNIT, mutation.source(), experiment.getRevision(), EnteredValue.DEFAULT_ONE);
        return new MutationResult(formatSetterSummary("input EQ", mutation.eq(), NoUnit.NO_UNIT)
                , new ReactionInputMutation.SetInputRowEQ(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowEQ.class)
class SetOutputRowEQHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowEQ> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowEQ mutation) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(row::getEq, row::setEq, mutation.eq(), NoUnit.NO_UNIT, mutation.source(), experiment.getRevision(), EnteredValue.DEFAULT_ONE);
        return new MutationResult(formatSetterSummary("output EQ", mutation.eq())
                , new ReactionOutputMutation.SetOutputRowEQ(mutation.anchor(), undo.value(), undo.source())
        );
    }
}
