package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionInputSampleMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.EnteredValueUndo;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;

import java.util.List;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMol.class)
class SetInputMolHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMol> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMol mutation) {
        EnteredValueUndo<MolUnit> undo = setEnteredValue(sample::getMol, sample::setMol, mutation.mol(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample mol", mutation.mol(), mutation.unit())
                , new ReactionInputSampleMutation.SetInputMol(mutation.anchor(), undo.value(), undo.unit(), undo.source()
        ));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputWeight.class)
class SetInputWeightHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputWeight> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputWeight mutation) {
        EnteredValueUndo<WeightUnit> undo = setEnteredValue(sample::getWeight, sample::setWeight, mutation.weight(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("input sample weight", mutation.weight(), mutation.unit())
                , new ReactionInputSampleMutation.SetInputWeight(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputHealthHazards.class)
class SetInputHealthHazardsHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputHealthHazards> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputHealthHazards mutation) {
        List<DictionaryItemRef> oldValue = sample.getHealthHazards();
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(formatSetterSummary("input sample health hazards", mutation.healthHazards())
                , new ReactionInputSampleMutation.SetInputHealthHazards(mutation.anchor(), oldValue)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputComment.class)
class SetInputCommentHandler extends AbstractReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputComment> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputComment mutation) {
        String oldValue = sample.getComment();
        sample.setComment(mutation.comment());
        return new MutationResult(formatSetterSummary("input sample comment", mutation.comment())
                , new ReactionInputSampleMutation.SetInputComment(mutation.anchor(), oldValue)
        );
    }
}
