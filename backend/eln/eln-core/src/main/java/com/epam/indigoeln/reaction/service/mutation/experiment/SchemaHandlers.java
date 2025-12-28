package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationContext;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationHelper;
import com.epam.indigoeln.reaction.service.mutation.ReactionMutationHandler;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;

@Dependent
@MutationHandlerFor(ReactionMutation.SetScheme.class)
class SetSchemeHandler implements ReactionMutationHandler<ReactionMutation.SetScheme> {

    @Inject
    IndigoAPI indigo;
    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.SetScheme mutation, MutationContext context) {
        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        IndigoReaction indigoReaction = indigo.loadReaction(mutation.molFile());
        for (IndigoMolecule reactant : indigoReaction.reactants()) {
            reaction.getInputs().add(mutationHelper.createInputLine(reaction, reactant, ReactionRole.REACTANT));
        }
        for (IndigoMolecule catalyst : indigoReaction.catalysts()) {
            reaction.getInputs().add(mutationHelper.createInputLine(reaction, catalyst, ReactionRole.CATALYST));
        }
        for (IndigoMolecule product : indigoReaction.products()) {
            reaction.getOutputs().add(mutationHelper.createOutputLine(reaction, product));
        }
        mutationHelper.adjustLimitingInput(reaction);
        reaction.setRxnfile(mutation.molFile());
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddEmptyInput.class)
class AddEmptyInputHandler implements ReactionMutationHandler<ReactionMutation.AddEmptyInput> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation, MutationContext context) {
        reaction.getInputs().add(mutationHelper.createInputLine(reaction, null, ReactionRole.REACTANT));
        mutationHelper.adjustLimitingInput(reaction);
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler implements ReactionMutationHandler<ReactionMutation.AddInput> {

    @Inject
    MutationHelper mutationHelper;
    @Inject
    CompoundService compoundService;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation, MutationContext context) {
        ReactionInput row = mutationHelper.createInputLine(reaction, null, ReactionRole.REACTANT);
        reaction.getInputs().add(row);
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        mutationHelper.setInputLineSample(row, sample, context);
        mutationHelper.adjustLimitingInput(reaction);
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler implements ReactionMutationHandler<ReactionMutation.ResolveInputs> {

    @Inject
    MutationHelper mutationHelper;
    @Inject
    CompoundService compoundService;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation, MutationContext context) {
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            mutationHelper.setInputLineSample(row, sample, context);
        });
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.RemoveInput.class)
class RemoveInputHandler implements ReactionMutationHandler<ReactionMutation.RemoveInput> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.RemoveInput mutation, MutationContext context) {
        ReactionInput input = model.locate(mutation.input());
        reaction.getInputs().remove(input);
        context.getAffectedRoles().add(input.getRole());
        mutationHelper.adjustLimitingInput(reaction);
    }
}
