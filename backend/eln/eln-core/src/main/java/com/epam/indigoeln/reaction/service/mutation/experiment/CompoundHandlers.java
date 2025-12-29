package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationHelper;
import com.epam.indigoeln.reaction.service.mutation.ReactionInputMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputMutationHandler;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltCode.class)
class SetInputRowSaltCodeHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltCode> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltCode mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        SaltCodeInfo saltCode = mutation.saltCode() != null ? mutationHelper.saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltCode.class)
class SetOutputRowSaltCodeHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltCode> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltCode mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        SaltCodeInfo saltCode = mutation.saltCode() != null ? mutationHelper.saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltEQ.class)
class SetInputRowSaltEQHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltEQ> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltEQ mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, mutationHelper.saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltEQ.class)
class SetOutputRowSaltEQHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltEQ> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltEQ mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, mutationHelper.saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundStereoisomerCode.class)
class SetInputCompoundStereoisomerCodeHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundStereoisomerCode> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundStereoisomerCode mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, mutationHelper.saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundStereoisomerCode.class)
class SetOutputCompoundStereoisomerCodeHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundStereoisomerCode> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundStereoisomerCode mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        row.setCompound(mutationHelper.doApplySetSaltCodeEQStereoisomerCode(row, mutationHelper.saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundMolWeight.class)
class SetInputCompoundMolWeightHandler implements ReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundMolWeight> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundMolWeight mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundMolWeight.class)
class SetOutputCompoundMolWeightHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundMolWeight> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundMolWeight mutation, com.epam.indigoeln.reaction.model.mutation.MutationContext context) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}
