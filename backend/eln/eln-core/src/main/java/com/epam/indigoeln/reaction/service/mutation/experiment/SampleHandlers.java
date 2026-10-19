package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getDensity(), sample::setDensity, mutation.density(), DensityUnit.G_ML, experiment.getRevision());
        setEnteredValue(sample.getMolarity(), sample::setMolarity, null, null, experiment.getRevision());
        return formatSetterSummary("input sample density", mutation.density(), DensityUnit.G_ML);
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getDensity(), sample::setDensity, mutation.density(), DensityUnit.G_ML, experiment.getRevision());
        setEnteredValue(sample.getMolarity(), sample::setMolarity, null, null, experiment.getRevision());
        return formatSetterSummary("batch density", mutation.density(), DensityUnit.G_ML);
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getMolarity(), sample::setMolarity, mutation.molarity(), model.getMolarityUnit(), experiment.getRevision());
        setEnteredValue(sample.getDensity(), sample::setDensity, null, null, experiment.getRevision());
        return formatSetterSummary("input sample molarity", mutation.molarity(), model.getMolarityUnit());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getMolarity(), sample::setMolarity, mutation.molarity(), model.getMolarityUnit(), experiment.getRevision());
        setEnteredValue(sample.getDensity(), sample::setDensity, null, null, experiment.getRevision());
        return formatSetterSummary("batch molarity", mutation.molarity(), model.getMolarityUnit());
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getPurity(), sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return formatSetterSummary("input sample purity", mutation.purity());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getPurity(), sample::setPurity, mutation.purity(), NoUnit.NO_UNIT, experiment.getRevision(), EnteredValue.DEFAULT_ONE_HUNDRED);
        return formatSetterSummary("batch purity", mutation.purity());
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getVolume(), sample::setVolume, mutation.volume(), model.getVolumeUnit(), experiment.getRevision());
        return formatSetterSummary("input sample volume", mutation.volume(), model.getVolumeUnit());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getVolume(), sample::setVolume, mutation.volume(), model.getVolumeUnit(), experiment.getRevision());
        return formatSetterSummary("batch volume", mutation.volume(), model.getVolumeUnit());
    }
}
