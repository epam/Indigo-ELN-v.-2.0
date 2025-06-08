package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputRole mutation) {
        ReactionInput row = model.locate(mutation);
        // TODO update scheme
        throw new UnsupportedOperationException(); // !!!
    }

    public void handle(ExperimentModel model, ReactionInputMutation.SetLimiting mutation) {
        ReactionInput row = model.locate(mutation);
        for (ReactionInput otherRow : row.getReaction().getInputs()) {
            otherRow.setLimiting(false);
        }
        row.setLimiting(true);
    }

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputEQ mutation) {
        ReactionInput row = model.locate(mutation);
        row.setEq(mutation.eq() != null ? EnteredValue.userLastEntered(mutation.eq(), NoUnit.NO_UNIT) : EnteredValue.DEFAULT_ONE);
    }
}
