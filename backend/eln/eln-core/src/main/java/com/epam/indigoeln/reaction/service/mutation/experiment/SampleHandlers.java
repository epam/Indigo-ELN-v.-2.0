package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation) {
        EnteredValueUndo<DensityUnit> undo = setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample density", mutation.density(), mutation.unit())
                , new ReactionInputSampleMutation.SetInputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation) {
        EnteredValueUndo<DensityUnit> undo = setEnteredValue(sample::getDensity, sample::setDensity, mutation.density(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch density", mutation.density(), mutation.unit())
                , new ReactionOutputSampleMutation.SetOutputDensity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation) {
        EnteredValueUndo<MolarityUnit> undo = setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample molarity", mutation.molarity(), mutation.unit())
                , new ReactionInputSampleMutation.SetInputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation) {
        EnteredValueUndo<MolarityUnit> undo = setEnteredValue(sample::getMolarity, sample::setMolarity, mutation.molarity(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch molarity", mutation.molarity(), mutation.unit())
                , new ReactionOutputSampleMutation.SetOutputMolarity(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source(), experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return new MutationResult(formatSetterSummary("input sample purity", mutation.purity())
                , new ReactionInputSampleMutation.SetInputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation) {
        EnteredValueUndo<NoUnit> undo = setEnteredValue(sample::getPurity, sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, mutation.source(), experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return new MutationResult(formatSetterSummary("batch purity", mutation.purity())
                , new ReactionOutputSampleMutation.SetOutputPurity(mutation.anchor(), undo.value(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation) {
        EnteredValueUndo<VolumeUnit> undo = setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample volume", mutation.volume(), mutation.unit())
                , new ReactionInputSampleMutation.SetInputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation) {
        EnteredValueUndo<VolumeUnit> undo = setEnteredValue(sample::getVolume, sample::setVolume, mutation.volume(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch volume", mutation.volume(), mutation.unit())
                , new ReactionOutputSampleMutation.SetOutputVolume(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.RemoveInput.class)
class RemoveInputHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.RemoveInput> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.RemoveInput mutation) {
        ReactionInput oldLimiting = reaction.getLimitingInput();
        Preconditions.checkState(oldLimiting != null);
        int position = reaction.getInputs().indexOf(row);
        reaction.getInputs().remove(row);
        affectedRoles.add(row.getRole());
        adjustLimitingInput(reaction);

        return new MutationResult("Remove input"
                , new ReactionMutation.UndoRemoveInput(reaction.getAnchor(), row, position, oldLimiting.getAnchor()));
    }
}
