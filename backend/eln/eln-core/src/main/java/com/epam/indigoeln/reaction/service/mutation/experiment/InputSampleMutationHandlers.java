package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionInputSampleMutationHandler;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMol.class)
class SetInputMolHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMol> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMol mutation, MutationContext context) {
        sample.setMol(EnteredValue.userLastEntered(mutation.mol(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputWeight.class)
class SetInputWeightHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputWeight> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputWeight mutation, MutationContext context) {
        sample.setWeight(EnteredValue.userLastEntered(mutation.weight(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputHealthHazards.class)
class SetInputHealthHazardsHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputHealthHazards> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputHealthHazards mutation, MutationContext context) {
        sample.setHealthHazards(mutation.healthHazards());
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputComment.class)
class SetInputCommentHandler implements ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputComment> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputComment mutation, MutationContext context) {
        sample.setComment(mutation.comment());
    }
}
