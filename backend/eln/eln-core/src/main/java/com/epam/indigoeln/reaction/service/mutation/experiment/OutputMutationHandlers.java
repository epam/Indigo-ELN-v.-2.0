package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE_HUNDRED;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ReactionOutputMutation.AddProductSample.class)
class AddProductSampleHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.AddProductSample> {

    @Override
    protected ReactionOutputMutation.AddProductSample doPrepareMutation(ExperimentEntity entity, ReactionOutputMutation.AddProductSample mutation, ExperimentMutationContext context) {
        return new ReactionOutputMutation.AddProductSample(
                mutation.anchor(),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : OutputSampleAnchor.create()
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.AddProductSample mutation, ExperimentMutationContext context) {
        OutputSampleAnchor anchor = checkNotNull(mutation.createdSampleAnchor());
        ReactionOutputSample.create(row, experiment.getName(), anchor, DEFAULT_ONE_HUNDRED);
        return new MutationResult("Add batch");
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowType.class)
class SetOutputRowTypeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowType> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowType mutation, ExperimentMutationContext context) {
        row.setType(mutation.outputType());
        // TODO add or remove to the next reaction, if changing to or from INTERMEDIATE type
        return new MutationResult(formatSetterSummary("output type", mutation.outputType()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowName.class)
class SetOutputRowNameHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowName> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowName mutation, ExperimentMutationContext context) {
        for (ReactionOutput otherRow : row.getReaction().getOutputs()) {
            if (otherRow != row) {
                InvalidRequestException.validate(!otherRow.getOutputName().equals(mutation.name()), "Output name " + mutation.name() + " is already used in this reaction");
            }
        }
        row.setOutputName(mutation.name());
        return new MutationResult(formatSetterSummary("output name", mutation.name()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowChemicalName.class)
class SetOutputRowChemicalNameHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowChemicalName> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowChemicalName mutation, ExperimentMutationContext context) {
        row.setChemicalName(mutation.chemicalName());
        return new MutationResult(formatSetterSummary("chemical name", mutation.chemicalName()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowIntended.class)
class SetOutputRowIntended extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowIntended> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowIntended mutation, ExperimentMutationContext context) {
        row.setIntended(mutation.intended());
        return new MutationResult("Product marked as " + (mutation.intended() ? "intended" : "not intended"));
    }
}
