package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ReactionMutation.SetScheme.class)
class SetSchemeHandler extends AbstractReactionMutationHandler<ReactionMutation.SetScheme> {

    @Inject
    IndigoAPI indigoAPI;

    @Nullable
    IndigoReaction reaction;

    @Override
    protected ReactionMutation.SetScheme doPrepareMutation(ExperimentEntity entity, ReactionMutation.SetScheme mutation) {
        int reactantCount = 0, catalystCount = 0, productCount = 0;
        if (mutation.rxnFile() != null) {
            reaction = indigoAPI.loadReaction(mutation.rxnFile());
            reactantCount = (int) StreamEx.of(reaction.reactants().iterator()).count();
            catalystCount = (int) StreamEx.of(reaction.catalysts().iterator()).count();
            productCount = (int) StreamEx.of(reaction.products().iterator()).count();
        }
        return new ReactionMutation.SetScheme(
                mutation.anchor(),
                mutation.rxnFile(),
                mutation.createdReactantAnchors() != null ? mutation.createdReactantAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> new InputAnchor(UUID.randomUUID())).toList(),
                mutation.createdReactantSampleAnchors() != null ? mutation.createdReactantSampleAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> new InputSampleAnchor(UUID.randomUUID())).toList(),
                mutation.createdCatalystAnchors() != null ? mutation.createdCatalystAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> new InputAnchor(UUID.randomUUID())).toList(),
                mutation.createdCatalystSampleAnchors() != null ? mutation.createdCatalystSampleAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> new InputSampleAnchor(UUID.randomUUID())).toList(),
                mutation.createdProductAnchors() != null ? mutation.createdProductAnchors() : IntStreamEx.range(productCount).mapToObj(x -> new OutputAnchor(UUID.randomUUID())).toList()
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.SetScheme mutation) {
        String oldRxnfile = reaction.getRxnfile();

        // TODO match into existing inputs/outputs
        List<ReactionInput> oldInputs = reaction.getInputs();
        reaction.setInputs(new ArrayList<>());
        List<ReactionOutput> oldOutputs = reaction.getOutputs();
        reaction.setOutputs(new ArrayList<>());

        if (mutation.rxnFile() != null) {
            IndigoReaction indigoReaction = indigoAPI.loadReaction(mutation.rxnFile());
            createInputs(checkNotNull(mutation.createdReactantAnchors()), checkNotNull(mutation.createdReactantSampleAnchors()), indigoReaction.reactants(), reaction, ReactionRole.REACTANT);
            createInputs(checkNotNull(mutation.createdCatalystAnchors()), checkNotNull(mutation.createdCatalystSampleAnchors()), indigoReaction.catalysts(), reaction, ReactionRole.CATALYST);
            createOutputs(experiment, checkNotNull(mutation.createdProductAnchors()), indigoReaction.products(), reaction);
        }
        adjustLimitingInput(reaction);
        reaction.setRxnfile(mutation.rxnFile());

        return new MutationResult("Update reaction scheme"
                , new ReactionMutation.UndoSetScheme(reaction.getAnchor(), oldRxnfile, oldInputs, oldOutputs));
    }

    private void createInputs(List<InputAnchor> createdAnchors, List<InputSampleAnchor> createdSampleAnchors, Iterable<IndigoMolecule> molecules, Reaction reaction, ReactionRole role) {
        Iterator<InputAnchor> anchorIterator = createdAnchors.iterator();
        Iterator<InputSampleAnchor> sampleAnchorIterator = createdSampleAnchors.iterator();
        for (IndigoMolecule reactant : molecules) {
            reaction.getInputs().add(createInputLine(reaction, reactant, role, anchorIterator.next(), sampleAnchorIterator.next()));
        }
    }

    private void createOutputs(ExperimentEntity experiment, List<OutputAnchor> createdAnchors, Iterable<IndigoMolecule> molecules, Reaction reaction) {
        Iterator<OutputAnchor> anchorIterator = createdAnchors.iterator();
        for (IndigoMolecule product : molecules) {
            reaction.getOutputs().add(createOutputLine(reaction, product, anchorIterator.next()));
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.UndoSetScheme.class)
class UndoSetSchemeHandler extends AbstractReactionMutationHandler<ReactionMutation.UndoSetScheme> {

    @Override
    protected MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.UndoSetScheme mutation) {
        reaction.setRxnfile(mutation.rxnFile());
        reaction.setInputs(mutation.inputs());
        reaction.getInputs().forEach(input -> input.setReaction(reaction));
        reaction.setOutputs(mutation.outputs());
        reaction.getOutputs().forEach(output -> output.setReaction(reaction));
        return new MutationResult("Undo set scheme", null);
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddEmptyInput.class)
class AddEmptyInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddEmptyInput> {

    @Override
    protected ReactionMutation.AddEmptyInput doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddEmptyInput mutation) {
        return new ReactionMutation.AddEmptyInput(
                mutation.anchor(),
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : new InputAnchor(UUID.randomUUID()),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : new InputSampleAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        reaction.getInputs().add(row);
        adjustLimitingInput(reaction);
        affectedRoles.add(row.getRole());
        return new MutationResult("Add empty input"
                , new ReactionInputMutation.RemoveInput(row.getAnchor())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddInput> {

    @Inject
    CompoundService compoundService;

    @Override
    protected ReactionMutation.AddInput doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddInput mutation) {
        return new ReactionMutation.AddInput(
                mutation.anchor(),
                mutation.sampleId(),
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : new InputAnchor(UUID.randomUUID()),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : new InputSampleAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        reaction.getInputs().add(row);
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        setInputLineSample(row, sample, mutation.createdSampleAnchor());
        adjustLimitingInput(reaction);

        return new MutationResult("Add input sample: " + getSampleIdentifier(sample)
                , new ReactionInputMutation.RemoveInput(row.getAnchor())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler extends AbstractReactionMutationHandler<ReactionMutation.ResolveInputs> {

    @Inject
    CompoundService compoundService;

    @Override
    protected ReactionMutation.ResolveInputs doPrepareMutation(ExperimentEntity entity, ReactionMutation.ResolveInputs mutation) {
        return new ReactionMutation.ResolveInputs(
                mutation.anchor(),
                mutation.inputSamples(),
                mutation.createdSampleAnchors() != null ? mutation.createdSampleAnchors() : EntryStream.of(mutation.inputSamples())
                        .mapValues(k -> new InputSampleAnchor(UUID.randomUUID()))
                        .toMap()
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation) {
        Map<InputAnchor, ReactionMutation.UndoResolveInputs.RowUndo> rowUndo = new HashMap<>();
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            var undo = setInputLineSample(row, sample, checkNotNull(mutation.createdSampleAnchors()).get(inputAnchor));
            rowUndo.put(inputAnchor, undo);
        });
        return new MutationResult("Resolve input samples"
                , new ReactionMutation.UndoResolveInputs(mutation.anchor(), rowUndo)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.UndoResolveInputs.class)
class UndoResolveInputsHandler extends AbstractReactionMutationHandler<ReactionMutation.UndoResolveInputs> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.UndoResolveInputs mutation) {
        mutation.rows().forEach((inputAnchor, undo) -> {
            ReactionInput row = model.locate(inputAnchor);
            row.setCompound(undo.compoundRef());
            row.setSamples(undo.samples());
            for (ReactionInputSample sample : row.getSamples()) {
                sample.setRow(row);
            }
            row.setChemicalName(undo.chemicalName());
            affectedRoles.add(row.getRole());
        });
        return new MutationResult("Undo resolve inputs", null);
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.UndoRemoveInput.class)
class UndoRemoveInputHandler extends AbstractReactionMutationHandler<ReactionMutation.UndoRemoveInput> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.UndoRemoveInput mutation) {
        reaction.getInputs().add(mutation.position(), mutation.input());
        mutation.input().setReaction(reaction);
        for (ReactionInput input : reaction.getInputs()) {
            input.setLimiting(input.getAnchor().equals(mutation.limitingInput()));
        }
        return new MutationResult("Undo remove input", null);
    }
}
