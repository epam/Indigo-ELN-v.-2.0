package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMol.class)
class SetInputMolHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMol> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMol mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setMol, mutation.mol(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample mol", mutation.mol(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputWeight.class)
class SetInputWeightHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputWeight> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputWeight mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setWeight, mutation.weight(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample weight", mutation.weight(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputHealthHazards.class)
class SetInputHealthHazardsHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputHealthHazards> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputHealthHazards mutation, ExperimentMutationContext context) {
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(formatSetterSummary("input sample health hazards", mutation.healthHazards()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputComment.class)
class SetInputCommentHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputComment> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputComment mutation, ExperimentMutationContext context) {
        sample.setComment(mutation.comment());
        return new MutationResult(formatSetterSummary("input sample comment", mutation.comment()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.RemoveInput.class)
class RemoveInputHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.RemoveInput> {

    @Override
    protected MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.RemoveInput mutation, ExperimentMutationContext context) {
        sample.delete();
        if (row.getSamples().isEmpty()) {
            row.delete();
        }

        context.getResponse().getMessages().add("Removed, press Ctrl-Z/Cmd-Z to undo");
        return new MutationResult("Remove input sample");
    }
}
