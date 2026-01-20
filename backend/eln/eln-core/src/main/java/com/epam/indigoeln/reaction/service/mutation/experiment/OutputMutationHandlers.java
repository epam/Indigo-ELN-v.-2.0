package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionOutputMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Dependent
@MutationHandlerFor(ReactionOutputMutation.AddProductSample.class)
class AddProductSampleHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.AddProductSample, MutationRedoInfo.AddOutputSample> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.AddProductSample mutation, MutationRedoInfo.@Nullable AddOutputSample redoInfo) {
        OutputSampleAnchor anchor = redoInfo != null ? redoInfo.anchor() : experiment.generateNextAnchor(OutputSampleAnchor.class);
        ReactionOutputSample sample = ReactionOutputSample.create(row, experiment.getName(), anchor);
        sample.setPurity(DEFAULT_ONE);
        row.getSamples().add(sample);
        return new MutationResult("Add batch"
                , new MutationRedoInfo.AddOutputSample(anchor)
                , new ReactionOutputSampleMutation.RemoveProductSample(anchor)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowType.class)
class SetOutputRowTypeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowType, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowType mutation, @Nullable MutationRedoInfo redoInfo) {
        ReactionOutputType oldType = row.getType();
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
        return new MutationResult(formatSetterSummary("output type", mutation.outputType())
                , null
                , new ReactionOutputMutation.SetOutputRowType(mutation.anchor(), oldType)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowName.class)
class SetOutputRowNameHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowName, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowName mutation, @Nullable MutationRedoInfo redoInfo) {
        for (ReactionOutput otherRow : row.getReaction().getOutputs()) {
            if (otherRow != row) {
                InvalidRequestException.validate(!otherRow.getOutputName().equals(mutation.name()), "Output name " + mutation.name() + " is already used in this reaction");
            }
        }
        String oldName = row.getOutputName();
        row.setOutputName(mutation.name());
        return new MutationResult(formatSetterSummary("output name", mutation.name())
                , null
                , new ReactionOutputMutation.SetOutputRowName(mutation.anchor(), oldName)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.UndoRemoveProductSample.class)
class UndoRemoveProductSampleHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.UndoRemoveProductSample, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.UndoRemoveProductSample mutation, @Nullable MutationRedoInfo redoInfo) {
        row.getSamples().add(mutation.sample());
        mutation.sample().setRow(row);
        return new MutationResult("Undo remove batch"
                , null
                , null
        );
    }
}
