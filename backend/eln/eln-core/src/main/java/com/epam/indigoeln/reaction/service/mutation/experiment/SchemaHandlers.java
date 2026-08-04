package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.google.common.base.Function;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Dependent
@MutationHandlerFor(ReactionMutation.SetScheme.class)
class SetSchemeHandler extends AbstractReactionMutationHandler<ReactionMutation.SetScheme> {

    private static final Comparator<ReactionRow> RXN_POSITION_COMPARATOR = Comparator.comparing(ReactionRow::getRxnPosition, Comparator.nullsLast(Comparator.naturalOrder()));
    private static final Comparator<ReactionInput> INPUT_COMPARATOR = Comparator.comparing(ReactionInput::getRole)
            .thenComparing(RXN_POSITION_COMPARATOR);

    @Inject
    ExperimentService experimentService;

    @Nullable
    IndigoReaction reaction;

    @Override
    protected ReactionMutation.SetScheme doPrepareMutation(ExperimentEntity entity, ReactionMutation.SetScheme mutation, ExperimentMutationContext context) {
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
                mutation.createdReactantAnchors() != null ? mutation.createdReactantAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> InputAnchor.create()).toList(),
                mutation.createdReactantSampleAnchors() != null ? mutation.createdReactantSampleAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> InputSampleAnchor.create()).toList(),
                mutation.createdCatalystAnchors() != null ? mutation.createdCatalystAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> InputAnchor.create()).toList(),
                mutation.createdCatalystSampleAnchors() != null ? mutation.createdCatalystSampleAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> InputSampleAnchor.create()).toList(),
                mutation.createdProductAnchors() != null ? mutation.createdProductAnchors() : IntStreamEx.range(productCount).mapToObj(x -> OutputAnchor.create()).toList()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.SetScheme mutation, ExperimentMutationContext context) {
        Map<Pair<String, Integer>, MoleculeLink<ReactionInput>> reactantLinks = new HashMap<>(), catalystLinks = new HashMap<>();
        Map<Pair<String, Integer>, MoleculeLink<ReactionOutput>> productLinks = new HashMap<>();
        if (reaction.getRxnfile() != null) {
            IndigoReaction indigoReaction = indigoAPI.loadReaction(reaction.getRxnfile());
            collectMoleculeLinks(indigoReaction.reactants(), reactantLinks, false);
            collectMoleculeLinks(indigoReaction.catalysts(), catalystLinks, false);
            for (ReactionInput row : reaction.getInputs()) {
                switch (row.getRole()) {
                    case REACTANT -> updateMoleculeLinkRow(reactantLinks, row);
                    case REAGENT, CATALYST -> updateMoleculeLinkRow(catalystLinks, row);
                }
            }
            collectMoleculeLinks(indigoReaction.products(), productLinks, false);
            for (ReactionOutput row : reaction.getOutputs()) {
                updateMoleculeLinkRow(productLinks, row);
            }
        }
        if (mutation.rxnFile() != null) {
            IndigoReaction indigoReaction = indigoAPI.loadReaction(mutation.rxnFile());
            collectMoleculeLinks(indigoReaction.reactants(), reactantLinks, true);
            collectMoleculeLinks(indigoReaction.catalysts(), catalystLinks, true);
            collectMoleculeLinks(indigoReaction.products(), productLinks, true);
        }

        Iterator<InputAnchor> createdReactantAnchors = checkNotNull(mutation.createdReactantAnchors()).iterator();
        Iterator<InputSampleAnchor> createdReactantSampleAnchors = checkNotNull(mutation.createdReactantSampleAnchors()).iterator();
        Iterator<InputAnchor> createdCatalystAnchors = checkNotNull(mutation.createdCatalystAnchors()).iterator();
        Iterator<InputSampleAnchor> createdCatalystSampleAnchors = checkNotNull(mutation.createdCatalystSampleAnchors()).iterator();
        Iterator<OutputAnchor> createdProductAnchors = checkNotNull(mutation.createdProductAnchors()).iterator();
        createRows(reactantLinks, link -> createInputLine(reaction, link.molecule, ReactionRole.REACTANT, createdReactantAnchors.next(), createdReactantSampleAnchors.next()));
        createRows(catalystLinks, link -> createInputLine(reaction, link.molecule, ReactionRole.REAGENT, createdCatalystAnchors.next(), createdCatalystSampleAnchors.next()));
        createRows(productLinks, link -> createOutputLine(reaction, link.molecule, true, createdProductAnchors.next()));

        reaction.setInputs(StreamEx.of(reaction.getInputs()).sorted(INPUT_COMPARATOR).toImmutableList());
        reaction.setOutputs(StreamEx.of(reaction.getOutputs()).sorted(RXN_POSITION_COMPARATOR).toImmutableList());

        reaction.setRxnfile(mutation.rxnFile());

        context.getResponse().setUnresolvedInputs(experimentService.analyzeRXN(reaction));

        return "Update reaction scheme";
    }

    private static <R extends ReactionRow> void updateMoleculeLinkRow(Map<Pair<String, Integer>, MoleculeLink<R>> links, R row) {
        if (row.getRxnPosition() != null) {
            for (MoleculeLink<R> link : links.values()) {
                if (row.getRxnPosition().equals(link.oldIndex)) {
                    link.row = row;
                    return;
                }
            }
            throw new IllegalStateException();
        }
    }

    private static <R extends ReactionRow> void createRows(Map<Pair<String, Integer>, MoleculeLink<R>> links, Function<MoleculeLink<R>, R> createFn) {
        for (MoleculeLink<R> link : links.values()) {
            if (link.newIndex == null) {
                if (link.row != null) {
                    link.row.delete();
                }
                continue;
            }
            if (link.row == null) {
                link.row = createFn.apply(link);
            }
            link.row.setRxnPosition(link.newIndex);
        }
    }

    private static <R extends ReactionRow> void collectMoleculeLinks(Iterable<IndigoMolecule> molecules, Map<Pair<String, Integer>, MoleculeLink<R>> map, boolean isNew) {
        int index = -1;
        Map<String, Integer> smilesCounts = new HashMap<>();
        for (IndigoMolecule molecule : molecules) {
            index++;
            String keySmiles = molecule.canonicalSmiles();
            Integer keyIndex = smilesCounts.getOrDefault(keySmiles, 0);
            smilesCounts.put(keySmiles, keyIndex + 1);
            MoleculeLink<R> link = map.computeIfAbsent(Pair.of(keySmiles, keyIndex), k -> new MoleculeLink<>(molecule));
            if (isNew) {
                link.newIndex = index;
            } else {
                link.oldIndex = index;
            }
            link.molecule = molecule;
        }
    }

    private static class MoleculeLink<R extends ReactionRow> {

        IndigoMolecule molecule;
        @Nullable
        Integer oldIndex;
        @Nullable
        Integer newIndex;
        @Nullable
        R row;

        MoleculeLink(IndigoMolecule molecule) {
            this.molecule = molecule;
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddEmptyInput.class)
class AddEmptyInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddEmptyInput> {

    @Override
    protected ReactionMutation.AddEmptyInput doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddEmptyInput mutation, ExperimentMutationContext context) {
        return new ReactionMutation.AddEmptyInput(
                mutation.anchor(),
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : InputAnchor.create(),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : InputSampleAnchor.create()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation, ExperimentMutationContext context) {
        createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        return "Add empty input";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddInput> {

    @Override
    protected ReactionMutation.AddInput doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddInput mutation, ExperimentMutationContext context) {
        return new ReactionMutation.AddInput(
                mutation.anchor(),
                mutation.sampleId(),
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : InputAnchor.create(),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : InputSampleAnchor.create()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation, ExperimentMutationContext context) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        setInputLineSample(row, sample, mutation.createdSampleAnchor(), context);

        return "Add input sample: " + getSampleIdentifier(sample);
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddNoProductSample.class)
class AddNoProductSampleHandler extends AbstractReactionMutationHandler<ReactionMutation.AddNoProductSample> {

    @Override
    protected ReactionMutation.AddNoProductSample doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddNoProductSample mutation, ExperimentMutationContext context) {
        return new ReactionMutation.AddNoProductSample(
                mutation.anchor(),
                mutation.createdOutputAnchor() != null ? mutation.createdOutputAnchor() : OutputAnchor.create(),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : OutputSampleAnchor.create()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddNoProductSample mutation, ExperimentMutationContext context) {
        CompoundRef compoundRef = compoundService.unknownCompoundRef();
        ReactionOutput row = ReactionOutput.create(reaction, ReactionOutputType.BY_PRODUCT, false, reaction.generateNextProductName(), mutation.createdOutputAnchor(), compoundRef, EnteredValue.DEFAULT_ONE);
        ReactionOutputSample.create(row, experiment.getName(), mutation.createdSampleAnchor(), EnteredValue.DEFAULT_ONE_HUNDRED);

        return "Add empty batch";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler extends AbstractReactionMutationHandler<ReactionMutation.ResolveInputs> {

    @Override
    protected ReactionMutation.ResolveInputs doPrepareMutation(ExperimentEntity entity, ReactionMutation.ResolveInputs mutation, ExperimentMutationContext context) {
        return new ReactionMutation.ResolveInputs(
                mutation.anchor(),
                mutation.inputSamples(),
                mutation.createdSampleAnchors() != null ? mutation.createdSampleAnchors() : EntryStream.of(mutation.inputSamples())
                        .mapValues(k -> InputSampleAnchor.create())
                        .toMap()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation, ExperimentMutationContext context) {
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            setInputLineSample(row, sample, checkNotNull(mutation.createdSampleAnchors()).get(inputAnchor), context);
        });
        return "Resolve input samples";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ImportSDF.class)
class ImportSDFHandler extends AbstractReactionMutationHandler<ReactionMutation.ImportSDF> {

    @Override
    protected ReactionMutation.ImportSDF doPrepareMutation(ExperimentEntity entity, ReactionMutation.ImportSDF mutation, ExperimentMutationContext context) {
        return new ReactionMutation.ImportSDF(
                mutation.anchor(),
                mutation.compoundIDs(),
                mutation.createdOutputAnchors() != null ? mutation.createdOutputAnchors() : mutation.compoundIDs().stream().map(x -> OutputAnchor.create()).toList(),
                mutation.createdSampleAnchors() != null ? mutation.createdSampleAnchors() : mutation.compoundIDs().stream().map(x -> OutputSampleAnchor.create()).toList()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ImportSDF mutation, ExperimentMutationContext context) {
        Iterator<OutputAnchor> anchorIt = mutation.createdOutputAnchors().iterator();
        Iterator<OutputSampleAnchor> sampleAnchorIt = mutation.createdSampleAnchors().iterator();
        for (UUID compoundID : mutation.compoundIDs()) {
            CompoundEntity compound = compoundService.getCompound(compoundID);
            ReactionOutput output = reaction.findOutput(compoundID);
            if (output == null) {
                CompoundRef compound1 = compoundService.virtualCompoundRef(compound);
                OutputAnchor anchor = anchorIt.next();
                output = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, false, reaction.generateNextProductName(), anchor, compound1, EnteredValue.DEFAULT_ONE);
            }
            ReactionOutputSample.create(output, experiment.getName(), sampleAnchorIt.next(), EnteredValue.DEFAULT_ONE_HUNDRED);
        }
        context.getResponse().getMessages().add(mutation.compoundIDs().size() + " samples imported from SDF");
        return "Import SDF (" + mutation.compoundIDs().size() + " samples)";
    }
}
