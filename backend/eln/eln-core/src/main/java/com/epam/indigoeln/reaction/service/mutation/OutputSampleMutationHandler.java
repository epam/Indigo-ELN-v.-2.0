package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@ApplicationScoped
public class OutputSampleMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputDensity mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setDensity(userLastEntered(mutation.density(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputMolarity mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setMolarity(userLastEntered(mutation.molarity(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputVolume mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setVolume(userLastEntered(mutation.volume(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputPurity mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setPurity(mutation.purity() != null ? userLastEntered(mutation.purity(), NoUnit.NO_UNIT) : DEFAULT_ONE);
    }

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputActualMol mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setActualMol(userLastEntered(mutation.actualMol(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.SetOutputActualWeight mutation) {
        ReactionOutputSample sample = model.locate(mutation);
        sample.setActualWeight(userLastEntered(mutation.actualWeight(), mutation.unit()));
    }
}
