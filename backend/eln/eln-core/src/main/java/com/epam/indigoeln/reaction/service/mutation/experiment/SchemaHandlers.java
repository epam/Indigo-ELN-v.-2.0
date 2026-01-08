package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.ReactionMutationHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@Dependent
@MutationHandlerFor(ReactionMutation.SetScheme.class)
class SetSchemeHandler implements ReactionMutationHandler<ReactionMutation.SetScheme, MutationRedoInfo.SetScheme> {

    @Inject
    IndigoAPI indigo;
    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.SetScheme mutation, MutationRedoInfo.@Nullable SetScheme redoInfo, MutationContext context) {
        String previousRxnfile = reaction.getRxnfile();

        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        List<Pair<Anchor.Input, Anchor.InputSample>> reactantAnchors = List.of(), catalystAnchors = List.of();
        List<Anchor.Output> productAnchors = List.of();

        if (mutation.molFile() != null) {
            IndigoReaction indigoReaction = indigo.loadReaction(mutation.molFile());
            reactantAnchors = createInputs(redoInfo != null ? redoInfo.reactantAnchors() : null, indigoReaction.reactants(), reaction, ReactionRole.REACTANT);
            catalystAnchors = createInputs(redoInfo != null ? redoInfo.catalystAnchors() : null, indigoReaction.catalysts(), reaction, ReactionRole.CATALYST);
            productAnchors = createOutputs(redoInfo != null ? redoInfo.productAnchors() : null, indigoReaction.products(), reaction);
        }
        mutationHelper.adjustLimitingInput(reaction);
        reaction.setRxnfile(mutation.molFile());

        return new MutationResult("Update reaction scheme"
                , new MutationRedoInfo.SetScheme(reactantAnchors, catalystAnchors, productAnchors)
                , new ReactionMutation.SetScheme(reaction.getAnchor(), previousRxnfile));
    }

    private List<Pair<Anchor.Input, Anchor.InputSample>> createInputs(List<Pair<Anchor.Input, Anchor.InputSample>> redoInfo, Iterable<IndigoMolecule> molecules, Reaction reaction, ReactionRole role) {
        List<Pair<Anchor.Input, Anchor.InputSample>> anchors = redoInfo != null
                ? redoInfo
                : StreamEx.of(molecules.iterator())
                        .map(m -> Pair.of(new Anchor.Input(reaction.getModel().generateNextAnchor()), new Anchor.InputSample(reaction.getModel().generateNextAnchor())))
                        .toList();
        Iterator<Pair<Anchor.Input, Anchor.InputSample>> anchorIterator = anchors.iterator();
        for (IndigoMolecule reactant : molecules) {
            reaction.getInputs().add(mutationHelper.createInputLine(reaction, reactant, role, anchorIterator.next()));
        }
        return anchors;
    }

    private List<Anchor.Output> createOutputs(List<Anchor.Output> redoInfo, Iterable<IndigoMolecule> molecules, Reaction reaction) {
        List<Anchor.Output> anchors = redoInfo != null
                ? redoInfo
                : StreamEx.of(molecules.iterator())
                        .map(m -> new Anchor.Output(reaction.getModel().generateNextAnchor()))
                        .toList();
        Iterator<Anchor.Output> anchorIterator = anchors.iterator();
        for (IndigoMolecule product : molecules) {
            reaction.getOutputs().add(mutationHelper.createOutputLine(reaction, product, anchorIterator.next()));
        }
        return anchors;
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddEmptyInput.class)
class AddEmptyInputHandler implements ReactionMutationHandler<ReactionMutation.AddEmptyInput, MutationRedoInfo.AddInput> {

    @Inject
    MutationHelper mutationHelper;

    // !!! move common handler logic into services; make handlers just call services
    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation, MutationRedoInfo.@Nullable AddInput redoInfo, MutationContext context) {
        ReactionInput row = mutationHelper.createInputLine(reaction, null, ReactionRole.REACTANT, redoInfo != null ? redoInfo.anchors() : null);
        reaction.getInputs().add(row);
        mutationHelper.adjustLimitingInput(reaction);
        context.getAffectedRoles().add(row.getRole());
        return new MutationResult("Add empty input"
                , new MutationRedoInfo.AddInput(Pair.of(row.getAnchor(), row.getSamples().getFirst().getAnchor()))
                , new ReactionInputMutation.RemoveInput(row.getAnchor())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler implements ReactionMutationHandler<ReactionMutation.AddInput, MutationRedoInfo.AddInput> {

    @Inject
    MutationHelper mutationHelper;
    @Inject
    CompoundService compoundService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation, MutationRedoInfo.@Nullable AddInput redoInfo, MutationContext context) {
        ReactionInput row = mutationHelper.createInputLine(reaction, null, ReactionRole.REACTANT, redoInfo != null ? redoInfo.anchors() : null);
        reaction.getInputs().add(row);
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        mutationHelper.setInputLineSample(row, sample, context, redoInfo != null ? redoInfo.anchors().b() : null);
        mutationHelper.adjustLimitingInput(reaction);

        return new MutationResult("Add input sample: " + mutationHelper.getSampleIdentifier(sample)
                , new MutationRedoInfo.AddInput(Pair.of(row.getAnchor(), row.getSamples().getFirst().getAnchor()))
                , new ReactionInputMutation.RemoveInput(row.getAnchor())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler implements ReactionMutationHandler<ReactionMutation.ResolveInputs, MutationRedoInfo.ResolveInputs> {

    @Inject
    MutationHelper mutationHelper;
    @Inject
    CompoundService compoundService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation, MutationRedoInfo.@Nullable ResolveInputs redoInfo, MutationContext context) {
        Map<Anchor.Input, Anchor.InputSample> sampleAnchors = redoInfo != null
                ? redoInfo.sampleAnchors()
                : EntryStream.of(mutation.inputSamples())
                        .mapValues(x -> new Anchor.InputSample(model.generateNextAnchor()))
                        .toMap();
        Map<Anchor.Input, ReactionMutation.UndoResolveInputs.RowUndo> rowUndo = new HashMap<>();
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            var undo = mutationHelper.setInputLineSample(row, sample, context, sampleAnchors.get(inputAnchor));
            rowUndo.put(inputAnchor, undo);
        });
        return new MutationResult("Resolve input samples"
                , new MutationRedoInfo.ResolveInputs(sampleAnchors)
                , new ReactionMutation.UndoResolveInputs(mutation.anchor(), rowUndo)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.UndoResolveInputs.class)
class UndoResolveInputsHandler implements ReactionMutationHandler<ReactionMutation.UndoResolveInputs, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.UndoResolveInputs mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        mutation.rows().forEach((inputAnchor, undo) -> {
            ReactionInput row = model.locate(inputAnchor);
            row.setCompound(undo.compoundRef());
            row.setSamples(undo.samples());
            for (ReactionInputSample sample : row.getSamples()) {
                sample.setRow(row);
            }
            row.setChemicalName(undo.chemicalName());
        });
        return new MutationResult("Undo resolve inputs"
                , null
                , null
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.UndoRemoveInput.class)
class UndoRemoveInputHandler implements ReactionMutationHandler<ReactionMutation.UndoRemoveInput, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.UndoRemoveInput mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        reaction.getInputs().add(mutation.input());
        mutation.input().setReaction(reaction);
        for (ReactionInput input : reaction.getInputs()) {
            input.setLimiting(input.getAnchor().equals(mutation.limitingInput()));
        }
        return new MutationResult("Undo remove input"
                , null
                , null
        );
    }
}
