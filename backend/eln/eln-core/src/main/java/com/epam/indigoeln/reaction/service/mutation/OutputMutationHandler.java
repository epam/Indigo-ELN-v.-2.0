package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.Dependent;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Dependent
public class OutputMutationHandler extends AbstractMutationHandler {

    public void handle(ReactionOutput row, ReactionOutputMutation.AddProductSample mutation) {
        ReactionOutputSample sample = ReactionOutputSample.create(experiment.getName(), row);
        sample.setPurity(DEFAULT_ONE);
        row.getSamples().add(sample);
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputRowType mutation) {
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputRowEQ mutation) {
        row.setEq(EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT, 1.0));
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputRowName mutation) {
        for (ReactionOutput otherRow : row.getReaction().getOutputs()) {
            InvalidRequestException.validate(otherRow == row || !otherRow.getName().equals(mutation.name()),"Output name " + mutation.name() + " is already used in this reaction");
        }
        row.setName(mutation.name());
    }
}
