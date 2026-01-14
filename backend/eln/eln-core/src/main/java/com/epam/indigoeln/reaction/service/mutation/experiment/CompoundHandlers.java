package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltCode.class)
class SetInputRowSaltCodeHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltCode, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltCode mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef oldSaltCode = row.getCompound().getSaltCode();
        SaltCodeInfo saltCode = mutation.saltCode() != null ? saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
        return new MutationResult(formatSetterSummary("input compound salt code", mutation.saltCode())
                , null
                , new ReactionInputMutation.SetInputRowSaltCode(mutation.anchor(), oldSaltCode));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltCode.class)
class SetOutputRowSaltCodeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltCode, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltCode mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef oldSaltCode = row.getCompound().getSaltCode();
        SaltCodeInfo saltCode = mutation.saltCode() != null ? saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
        return new MutationResult(formatSetterSummary("output compound salt code", mutation.saltCode())
                , null
                , new ReactionOutputMutation.SetOutputRowSaltCode(mutation.anchor(), oldSaltCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltEQ.class)
class SetInputRowSaltEQHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltEQ, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltEQ mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        Double oldValue = row.getCompound().getSaltEQ();
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
        return new MutationResult(formatSetterSummary("input compound salt EQ", mutation.saltEQ())
                , null
                , new ReactionInputMutation.SetInputRowSaltEQ(mutation.anchor(), oldValue));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltEQ.class)
class SetOutputRowSaltEQHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltEQ, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltEQ mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        Double oldValue = row.getCompound().getSaltEQ();
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
        return new MutationResult(formatSetterSummary("output compound salt EQ", mutation.saltEQ())
                , null
                , new ReactionOutputMutation.SetOutputRowSaltEQ(mutation.anchor(), oldValue)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundStereoisomerCode.class)
class SetInputCompoundStereoisomerCodeHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundStereoisomerCode, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundStereoisomerCode mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef oldStereoisomerCode = row.getCompound().getStereoisomerCode();
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
        return new MutationResult(formatSetterSummary("input compound stereoisomer code", mutation.stereoisomerCode())
                , null
                , new ReactionInputMutation.SetInputCompoundStereoisomerCode(mutation.anchor(), oldStereoisomerCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundStereoisomerCode.class)
class SetOutputCompoundStereoisomerCodeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundStereoisomerCode, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundStereoisomerCode mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef oldStereoisomerCode = row.getCompound().getStereoisomerCode();
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
        return new MutationResult(formatSetterSummary("output compound stereoisomer code", mutation.stereoisomerCode())
                , null
                , new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(mutation.anchor(), oldStereoisomerCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundMolWeight.class)
class SetInputCompoundMolWeightHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundMolWeight, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundMolWeight mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            EnteredValueUndo<MolWeightUnit> undo = setEnteredValue(c::getMolWeight, c::setMolWeight, mutation.molWeight(), MolWeightUnit.G_PER_MOL, mutation.source());
            return new MutationResult(formatSetterSummary("input compound mol weight", mutation.molWeight(), MolWeightUnit.G_PER_MOL)
                , null
                , new ReactionInputMutation.SetInputCompoundMolWeight(mutation.anchor(), undo.value(), undo.source())
            );
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundMolWeight.class)
class SetOutputCompoundMolWeightHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundMolWeight, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundMolWeight mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            EnteredValueUndo<MolWeightUnit> undo = setEnteredValue(c::getMolWeight, c::setMolWeight, mutation.molWeight(), MolWeightUnit.G_PER_MOL, mutation.source());
            return new MutationResult(formatSetterSummary("output compound mol weight", mutation.molWeight(), MolWeightUnit.G_PER_MOL)
                    , null
                    , new ReactionOutputMutation.SetOutputCompoundMolWeight(mutation.anchor(), undo.value(), undo.source())
            );
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}
