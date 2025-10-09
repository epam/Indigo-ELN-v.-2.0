package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class OutputMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.AddProductSample mutation) {
        ReactionOutputSample sample = ReactionOutputSample.create(row);
        sample.setPurity(DEFAULT_ONE);
        row.getSamples().add(sample);
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputType mutation) {
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputEQ mutation) {
        row.setEq(EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT, 1.0));
    }
}
