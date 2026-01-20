package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;
import org.jspecify.annotations.Nullable;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<DensityUnit> undo = setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("input sample density", mutation.density(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<DensityUnit> undo = setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("batch density", mutation.density(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<MolarityUnit> undo = setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("input sample molarity", mutation.molarity(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<MolarityUnit> undo = setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("batch molarity", mutation.molarity(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source());
        return new MutationResult(formatSetterSummary("input sample purity", mutation.purity())
                , null
                , new ReactionInputSampleMutation.SetInputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source());
        return new MutationResult(formatSetterSummary("batch purity", mutation.purity())
                , null
                , new ReactionOutputSampleMutation.SetOutputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<VolumeUnit> undo = setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("input sample volume", mutation.volume(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation, @Nullable MutationRedoInfo redoInfo) {
        EnteredValueUndo<VolumeUnit> undo = setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("batch volume", mutation.volume(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.RemoveInput.class)
class RemoveInputHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.RemoveInput, MutationRedoInfo> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.RemoveInput mutation, @Nullable MutationRedoInfo redoInfo) {
        ReactionInput oldLimiting = reaction.getLimitingInput();
        Preconditions.checkState(oldLimiting != null);
        int position = reaction.getInputs().indexOf(row);
        reaction.getInputs().remove(row);
        affectedRoles.add(row.getRole());
        adjustLimitingInput(reaction);

        return new MutationResult("Remove input"
                , null
                , new ReactionMutation.UndoRemoveInput(reaction.getAnchor(), row, position, oldLimiting.getAnchor()));
    }
}
