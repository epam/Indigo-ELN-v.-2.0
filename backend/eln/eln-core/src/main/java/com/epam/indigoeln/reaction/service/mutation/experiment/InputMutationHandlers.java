package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.service.mutation.MutationContext;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionInputMutationHandler;
import jakarta.enterprise.context.Dependent;
import one.util.streamex.StreamEx;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowLimiting.class)
class SetInputRowLimitingHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowLimiting> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowLimiting mutation, MutationContext context) {
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowChemicalName.class)
class SetInputRowChemicalNameHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowChemicalName> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowChemicalName mutation, MutationContext context) {
        row.setChemicalName(mutation.chemicalName());
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowMol.class)
class SetInputRowMolHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowMol> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowMol mutation, MutationContext context) {
        row.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.molUnit()));
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowRole.class)
class SetInputRowRoleHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowRole> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowRole mutation, MutationContext context) {
        StreamEx.of(row.getReaction().getInputs())
                .filter(x -> x != row && x.getRole() == row.getRole() && x.getCompound().equals(row.getCompound()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalStateException("Input with the same role and compound already exists");
                });

        context.getAffectedRoles().add(row.getRole());
        context.getAffectedRoles().add(mutation.role());
        row.setRole(mutation.role());
    }
}
