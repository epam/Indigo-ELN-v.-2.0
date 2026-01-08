package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<DensityUnit> undo = mutationHelper.setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("input sample density", mutation.density(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<DensityUnit> undo = mutationHelper.setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch density", mutation.density(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<MolarityUnit> undo = mutationHelper.setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("input sample molarity", mutation.molarity(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<MolarityUnit> undo = mutationHelper.setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch molarity", mutation.molarity(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<NoUnit> undo = mutationHelper.setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("input sample purity", mutation.purity())
                , null
                , new ReactionInputSampleMutation.SetInputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<NoUnit> undo = mutationHelper.setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch purity", mutation.purity())
                , null
                , new ReactionOutputSampleMutation.SetOutputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<VolumeUnit> undo = mutationHelper.setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("input sample volume", mutation.volume(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<VolumeUnit> undo = mutationHelper.setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch volume", mutation.volume(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.RemoveInput.class)
class RemoveInputHandler implements ReactionInputMutationHandler<ReactionInputMutation.RemoveInput, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.RemoveInput mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        ReactionInput oldLimiting = reaction.getLimitingInput();
        Preconditions.checkState(oldLimiting != null);
        reaction.getInputs().remove(row);
        context.getAffectedRoles().add(row.getRole());
        mutationHelper.adjustLimitingInput(reaction);

        return new MutationResult("Remove input"
                , null
                , new ReactionMutation.UndoRemoveInput(reaction.getAnchor(), row, oldLimiting.getAnchor()));
    }
}
