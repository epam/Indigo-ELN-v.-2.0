package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionInputMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.EnteredValueUndo;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowLimiting.class)
class SetInputRowLimitingHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowLimiting, MutationRedoInfo> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowLimiting mutation, @Nullable MutationRedoInfo redoInfo) {
        Preconditions.checkState(row.getReaction().getLimitingInput() != null);
        InputAnchor oldLimiting = row.getReaction().getLimitingInput().getAnchor();
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
        return new MutationResult("Change limiting input"
                , null
                , new ReactionInputMutation.SetInputRowLimiting(oldLimiting)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowChemicalName.class)
class SetInputRowChemicalNameHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowChemicalName, MutationRedoInfo> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowChemicalName mutation, @Nullable MutationRedoInfo redoInfo) {
        String old = row.getChemicalName();
        row.setChemicalName(mutation.chemicalName());
        return new MutationResult(formatSetterSummary("input chemical name", mutation.chemicalName())
                , null
                , new ReactionInputMutation.SetInputRowChemicalName(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowMol.class)
class SetInputRowMolHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowMol, MutationRedoInfo> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowMol mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<MolUnit> undo = setEnteredValue(row::getMol, row::setMol, mutation.mol(), mutation.molUnit(), mutation.source());
        return new MutationResult(formatSetterSummary("input mol", mutation.mol(), mutation.molUnit())
                , null
                , new ReactionInputMutation.SetInputRowMol(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowRole.class)
class SetInputRowRoleHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowRole, MutationRedoInfo> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowRole mutation, @Nullable MutationRedoInfo redoInfo) {
        ReactionRole oldRole = row.getRole();
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        affectedRoles.add(row.getRole());
        affectedRoles.add(mutation.role());
        row.setRole(mutation.role());
        return new MutationResult(formatSetterSummary("input role", mutation.role())
                , null
                , new ReactionInputMutation.SetInputRowRole(mutation.anchor(), oldRole)
        );
    }
}
