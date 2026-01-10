package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputMol.class)
class SetInputMolHandler extends ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputMol, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputMol mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<MolUnit> undo = setEnteredValue(sample::getMol, sample::setMol, mutation.mol(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("input sample mol", mutation.mol(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputMol(mutation.anchor(), undo.value(), undo.unit(), undo.source()
        ));
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputWeight.class)
class SetInputWeightHandler extends ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputWeight, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputWeight mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<WeightUnit> undo = setEnteredValue(sample::getWeight, sample::setWeight, mutation.weight(), mutation.unit(), mutation.source());
        return new MutationResult(formatSetterSummary("input sample weight", mutation.weight(), mutation.unit())
                , null
                , new ReactionInputSampleMutation.SetInputWeight(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputHealthHazards.class)
class SetInputHealthHazardsHandler extends ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputHealthHazards, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputHealthHazards mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<DictionaryItemRef> oldValue = sample.getHealthHazards();
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(formatSetterSummary("input sample health hazards", mutation.healthHazards())
                , null
                , new ReactionInputSampleMutation.SetInputHealthHazards(mutation.anchor(), oldValue)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputSampleMutation.SetInputComment.class)
class SetInputCommentHandler extends ReactionInputSampleMutationHandler<ReactionInputSampleMutation.SetInputComment, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputSample sample, ReactionInputSampleMutation.SetInputComment mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        String oldValue = sample.getComment();
        sample.setComment(mutation.comment());
        return new MutationResult(formatSetterSummary("input sample comment", mutation.comment())
                , null
                , new ReactionInputSampleMutation.SetInputComment(mutation.anchor(), oldValue)
        );
    }
}
