package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import one.util.streamex.StreamEx;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowLimiting.class)
class SetInputRowLimitingHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowLimiting> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowLimiting mutation, ExperimentMutationContext context) {
        Preconditions.checkState(row.getReaction().getLimitingInput() != null);
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
        return new MutationResult("Change limiting input");
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowChemicalName.class)
class SetInputRowChemicalNameHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowChemicalName> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowChemicalName mutation, ExperimentMutationContext context) {
        row.setChemicalName(mutation.chemicalName());
        return new MutationResult(formatSetterSummary("input chemical name", mutation.chemicalName()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowMol.class)
class SetInputRowMolHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowMol> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowMol mutation, ExperimentMutationContext context) {
        setEnteredValue(row::setMol, mutation.mol(), mutation.molUnit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input mol", mutation.mol(), mutation.molUnit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowRole.class)
class SetInputRowRoleHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowRole> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowRole mutation, ExperimentMutationContext context) {
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        context.setSchemaAffected(true);
        row.setRole(mutation.role());
        return new MutationResult(formatSetterSummary("input role", mutation.role()));
    }
}
