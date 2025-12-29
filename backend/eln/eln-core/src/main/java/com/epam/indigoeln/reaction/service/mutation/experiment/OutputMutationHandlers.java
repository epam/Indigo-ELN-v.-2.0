package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputMutationHandler;
import jakarta.enterprise.context.Dependent;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Dependent
@MutationHandlerFor(ReactionOutputMutation.AddProductSample.class)
class AddProductSampleHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.AddProductSample> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.AddProductSample mutation, MutationContext context) {
        ReactionOutputSample sample = ReactionOutputSample.create(experiment.getName(), row);
        sample.setPurity(DEFAULT_ONE);
        row.getSamples().add(sample);
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowType.class)
class SetOutputRowTypeHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowType> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowType mutation, MutationContext context) {
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowName.class)
class SetOutputRowNameHandler implements ReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowName> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowName mutation, MutationContext context) {
        for (ReactionOutput otherRow : row.getReaction().getOutputs()) {
            InvalidRequestException.validate(otherRow == row || !otherRow.getOutputName().equals(mutation.name()),"Output name " + mutation.name() + " is already used in this reaction");
        }
        row.setOutputName(mutation.name());
    }
}
