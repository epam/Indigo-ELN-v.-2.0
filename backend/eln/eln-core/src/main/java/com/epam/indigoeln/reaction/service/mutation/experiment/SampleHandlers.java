package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.service.mutation.MutationContext;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionInputSampleMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputSampleMutationHandler;
import jakarta.enterprise.context.Dependent;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputDensity.class)
class SetInputDensityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputDensity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation, MutationContext context) {
        sample.setDensity(EnteredValue.userLastEntered(mutation.density(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputDensity.class)
class SetOutputDensityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputDensity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation, MutationContext context) {
        sample.setDensity(userLastEntered(mutation.density(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMolarity.class)
class SetInputMolarityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMolarity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation, MutationContext context) {
        sample.setMolarity(EnteredValue.userLastEntered(mutation.molarity(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolarity.class)
class SetOutputMolarityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolarity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation, MutationContext context) {
        sample.setMolarity(userLastEntered(mutation.molarity(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputPurity.class)
class SetInputPurityHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputPurity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation, MutationContext context) {
        sample.setPurity(EnteredValue.userLastEntered(mutation.purity(), NoUnit.NO_UNIT, 1.0));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurity.class)
class SetOutputPurityHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurity> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation, MutationContext context) {
        sample.setPurity(userLastEntered(mutation.purity(), NoUnit.NO_UNIT, 1.0));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputVolume.class)
class SetInputVolumeHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputVolume> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation, MutationContext context) {
        sample.setVolume(EnteredValue.userLastEntered(mutation.volume(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputVolume.class)
class SetOutputVolumeHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputVolume> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation, MutationContext context) {
        sample.setVolume(userLastEntered(mutation.volume(), mutation.unit()));
    }
}
