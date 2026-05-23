package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;

@Dependent
@Priority(ExperimentModelMutationListener.DEFAULT_PRIORITY)
public class AdjustLimitingInputListener implements ExperimentModelMutationListener {

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentModel model, ExperimentMutationContext context) {
        for (Reaction reaction : model.getReactions()) {
            if (!reaction.getInputs().isEmpty()) {
                ReactionInput limiting = reaction.getLimitingInput();
                if (limiting == null) {
                    limiting = reaction.getInputs().getFirst();
                }
                for (ReactionInput input : reaction.getInputs()) {
                    input.setLimiting(input == limiting);
                }
            }
        }
    }
}
