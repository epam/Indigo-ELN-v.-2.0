package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setDensity, mutation.density(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample density", mutation.density(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setDensity, mutation.density(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch density", mutation.density(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setMolarity, mutation.molarity(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample molarity", mutation.molarity(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setMolarity, mutation.molarity(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch molarity", mutation.molarity(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return new MutationResult(formatSetterSummary("input sample purity", mutation.purity()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return new MutationResult(formatSetterSummary("batch purity", mutation.purity()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setVolume, mutation.volume(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample volume", mutation.volume(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setVolume, mutation.volume(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch volume", mutation.volume(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.RemoveInput.class)
class RemoveInputHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.RemoveInput> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.RemoveInput mutation, ExperimentMutationContext context) {
        row.delete();
        context.setSchemaAffected(true);
        adjustLimitingInput(reaction);

        return new MutationResult("Remove input");
    }
}
