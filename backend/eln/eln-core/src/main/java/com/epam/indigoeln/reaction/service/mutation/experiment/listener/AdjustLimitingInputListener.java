package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.enterprise.context.Dependent;

@Dependent
//@MutationListener(priority = xxx)
public class AdjustLimitingInputListener extends ExperimentMutationListener<ExperimentMutation> {

    @Override
    public void afterHandle(ExperimentEntity experiment, ExperimentMutation mutation, ExperimentMutationContext context) {
        if (experiment.getModelObj() != null) {
            for (Reaction reaction : experiment.getModelObj().getReactions()) {
                if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
                    reaction.getInputs().getFirst().setLimiting(true);
                }
            }
        }
    }
}
