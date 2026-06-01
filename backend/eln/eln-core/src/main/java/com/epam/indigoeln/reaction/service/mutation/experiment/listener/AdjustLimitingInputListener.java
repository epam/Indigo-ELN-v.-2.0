package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;

@Dependent
@Priority(ExperimentModelMutationListener.DEFAULT_PRIORITY)
public class AdjustLimitingInputListener implements ExperimentModelMutationListener {

    @Override
    public void beforeRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        for (Reaction reaction : model.getReactions()) {
            if (reaction.getInputs().isEmpty()) {
                reaction.setLimitingAnchor(null);
            } else if (reaction.getLimitingInput() == null) {
                reaction.setLimitingAnchor(reaction.getInputs().getFirst().getAnchor());
            }
        }
    }
}
