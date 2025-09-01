package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class OutputMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionOutputMutation.AddProductSample mutation) {
        ReactionOutput row = (ReactionOutput) model.locate(mutation);
        ReactionOutputSample sample = new ReactionOutputSample(row, UUID.randomUUID());
        sample.setPurity(DEFAULT_ONE);
        row.getSamples().add(sample);
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputType mutation) {
        ReactionOutput row = model.locate(mutation);
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputEQ mutation) {
        ReactionOutput row = model.locate(mutation);
        row.setEq(mutation.eq() != null ? EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT) : DEFAULT_ONE);
    }
}
