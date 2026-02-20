package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionOutputMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;

import java.util.UUID;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE_HUNDRED;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ReactionOutputMutation.AddProductSample.class)
class AddProductSampleHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.AddProductSample> {

    @Override
    protected ReactionOutputMutation.AddProductSample doPrepareMutation(ExperimentEntity entity, ReactionOutputMutation.AddProductSample mutation) {
        return new ReactionOutputMutation.AddProductSample(
                mutation.anchor(),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : new OutputSampleAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.AddProductSample mutation) {
        OutputSampleAnchor anchor = checkNotNull(mutation.createdSampleAnchor());
        ReactionOutputSample sample = ReactionOutputSample.create(row, experiment.getName(), anchor);
        sample.setPurity(DEFAULT_ONE_HUNDRED);
        row.getSamples().add(sample);
        return new MutationResult("Add batch"
                , new ReactionOutputSampleMutation.RemoveProductSample(anchor)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowType.class)
class SetOutputRowTypeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowType> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowType mutation) {
        ReactionOutputType oldType = row.getType();
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
        return new MutationResult(formatSetterSummary("output type", mutation.outputType())
                , new ReactionOutputMutation.SetOutputRowType(mutation.anchor(), oldType)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowName.class)
class SetOutputRowNameHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowName> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowName mutation) {
        for (ReactionOutput otherRow : row.getReaction().getOutputs()) {
            if (otherRow != row) {
                InvalidRequestException.validate(!otherRow.getOutputName().equals(mutation.name()), "Output name " + mutation.name() + " is already used in this reaction");
            }
        }
        String oldName = row.getOutputName();
        row.setOutputName(mutation.name());
        return new MutationResult(formatSetterSummary("output name", mutation.name())
                , new ReactionOutputMutation.SetOutputRowName(mutation.anchor(), oldName)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.UndoRemoveProductSample.class)
class UndoRemoveProductSampleHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.UndoRemoveProductSample> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.UndoRemoveProductSample mutation) {
        row.getSamples().add(mutation.sample());
        mutation.sample().setRow(row);
        return new MutationResult("Undo remove batch", null);
    }
}
