package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputSampleMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputDensity mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setDensity(EnteredValue.userLastEntered(mutation.density(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputMolarity mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setMolarity(EnteredValue.userLastEntered(mutation.molarity(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputVolume mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setVolume(EnteredValue.userLastEntered(mutation.volume(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputPurity mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setPurity(mutation.purity() != null ? EnteredValue.userLastEntered(mutation.purity(), NoUnit.NO_UNIT) : EnteredValue.DEFAULT_ONE);
    }

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputMol mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionInputSampleMutation.SetInputWeight mutation) {
        ReactionInputSample sample = model.locate(mutation);
        sample.setWeight(EnteredValue.userLastEntered(mutation.weight(), mutation.unit()));
    }
}
