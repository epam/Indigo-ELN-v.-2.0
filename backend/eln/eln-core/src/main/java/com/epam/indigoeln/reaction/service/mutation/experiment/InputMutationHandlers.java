package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowLimiting.class)
class SetInputRowLimitingHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowLimiting, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowLimiting mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        Preconditions.checkState(row.getReaction().getLimitingInput() != null);
        Anchor.Input oldLimiting = row.getReaction().getLimitingInput().getAnchor();
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
class SetInputRowChemicalNameHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowChemicalName, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowChemicalName mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        String old = row.getChemicalName();
        row.setChemicalName(mutation.chemicalName());
        return new MutationResult(mutationHelper.formatSetterSummary("input chemical name", mutation.chemicalName())
                , null
                , new ReactionInputMutation.SetInputRowChemicalName(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowMol.class)
class SetInputRowMolHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowMol, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowMol mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        // TODO update when multi-sample is supported
        ReactionInputSample sample = row.getSamples().getFirst();
        EnteredValueUndo<MolUnit> undo = mutationHelper.setEnteredValue(sample::getMol, sample::setMol, mutation.mol(), mutation.molUnit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("input mol", mutation.mol(), mutation.molUnit())
                , null
                , new ReactionInputMutation.SetInputRowMol(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowRole.class)
class SetInputRowRoleHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowRole, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowRole mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        ReactionRole oldRole = row.getRole();
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        context.getAffectedRoles().add(row.getRole());
        context.getAffectedRoles().add(mutation.role());
        row.setRole(mutation.role());
        return new MutationResult(mutationHelper.formatSetterSummary("input role", mutation.role())
                , null
                , new ReactionInputMutation.SetInputRowRole(mutation.anchor(), oldRole)
        );
    }
}
