package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.Dependent;

@Dependent
public class InputSampleMutationHandler extends AbstractMutationHandler {

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputDensity mutation) {
        sample.setDensity(EnteredValue.userLastEntered(mutation.density(), mutation.unit()));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputMolarity mutation) {
        sample.setMolarity(EnteredValue.userLastEntered(mutation.molarity(), mutation.unit()));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputVolume mutation) {
        sample.setVolume(EnteredValue.userLastEntered(mutation.volume(), mutation.unit()));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputPurity mutation) {
        sample.setPurity(EnteredValue.userLastEntered(mutation.purity(), NoUnit.NO_UNIT, 1.0));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputMol mutation) {
        sample.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.unit()));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputWeight mutation) {
        sample.setWeight(EnteredValue.userLastEntered(mutation.weight(), mutation.unit()));
    }

    public void handle(ReactionInputSample sample, ReactionInputSampleMutation.SetInputHealthHazards mutation) {
        sample.setHealthHazards(mutation.healthHazards());
        dictionariesAffected = true;
    }
}
