package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.enterprise.context.Dependent;

@Dependent
//@MutationListener(priority = xxx)
public class AdjustLimitingInputListener extends ExperimentModelMutationListener {

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        for (Reaction reaction : model.getReactions()) {
            if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
                reaction.getInputs().getFirst().setLimiting(true);
            }
        }
    }
}
