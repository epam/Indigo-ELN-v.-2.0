package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.ReactionRow;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

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
        //noinspection ConstantValue
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

        Iterator<InputAnchor> createdReactantAnchors = mutation.createdReactantAnchors().iterator();
        Iterator<InputSampleAnchor> createdReactantSampleAnchors = mutation.createdReactantSampleAnchors().iterator();
        Iterator<InputAnchor> createdCatalystAnchors = mutation.createdCatalystAnchors().iterator();
        Iterator<InputSampleAnchor> createdCatalystSampleAnchors = mutation.createdCatalystSampleAnchors().iterator();
        Iterator<OutputAnchor> createdProductAnchors = mutation.createdProductAnchors().iterator();
        createRows(reactantLinks, link -> {
            ReactionInput row = createInputLine(reaction, link.molecule, ReactionRole.REACTANT, createdReactantAnchors.next());
            ReactionInputSample.create(row, createdReactantSampleAnchors.next(), SampleSource.VIRTUAL, null, EnteredValue.DEFAULT_ONE_HUNDRED);
            return row;
        });
        createRows(catalystLinks, link -> {
            ReactionInput row = createInputLine(reaction, link.molecule, ReactionRole.REAGENT, createdCatalystAnchors.next());
            ReactionInputSample.create(row, createdCatalystSampleAnchors.next(), SampleSource.VIRTUAL, null, EnteredValue.DEFAULT_ONE_HUNDRED);
            return row;
        });
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
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation, ExperimentMutationContext context) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, mutation.createdInputAnchor());
        ReactionInputSample.create(row, mutation.createdSampleAnchor(), SampleSource.VIRTUAL, null, EnteredValue.DEFAULT_ONE_HUNDRED);
        return "Add empty input";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddInput> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation, ExperimentMutationContext context) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, mutation.createdInputAnchor());
        setInputLineSample(row, mutation.sample(), mutation.createdSampleAnchor(), context);

        return "Add input sample: " + MoreObjects.firstNonNull(mutation.sample().getSampleKey(), mutation.sample().getNbkBatchNumber());
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddNoProductSample.class)
class AddNoProductSampleHandler extends AbstractReactionMutationHandler<ReactionMutation.AddNoProductSample> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddNoProductSample mutation, ExperimentMutationContext context) {
        CompoundRef compoundRef = compoundService.unknownCompoundRef();
        ReactionOutput row = ReactionOutput.create(reaction, ReactionOutputType.BY_PRODUCT, false, reaction.generateNextProductName(), mutation.createdOutputAnchor(), compoundRef, EnteredValue.DEFAULT_ONE);
        ReactionOutputSample.create(row, experiment.getName(), mutation.createdSampleAnchor(), SampleSource.VIRTUAL, null, EnteredValue.DEFAULT_ONE_HUNDRED);

        return "Add empty batch";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler extends AbstractReactionMutationHandler<ReactionMutation.ResolveInputs> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation, ExperimentMutationContext context) {
        validate(mutation.createdSampleAnchors().keySet().equals(mutation.inputSamples().keySet()), "createdSampleAnchors must have the same keys as inputSamples");
        mutation.inputSamples().forEach((inputAnchor, sample) -> {
            ReactionInput row = model.locate(inputAnchor);
            setInputLineSample(row, sample, mutation.createdSampleAnchors().get(inputAnchor), context);
        });
        return "Resolve input samples";
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ImportSDF.class)
class ImportSDFHandler extends AbstractReactionMutationHandler<ReactionMutation.ImportSDF> {

    @Override
    protected ReactionMutation.ImportSDF doPrepareMutation(ExperimentEntity entity, ReactionMutation.ImportSDF mutation, ExperimentMutationContext context) {
        //noinspection ConstantValue
        return new ReactionMutation.ImportSDF(
                mutation.anchor(),
                mutation.compoundIDs(),
                mutation.samples(),
                mutation.createdOutputAnchors() != null ? mutation.createdOutputAnchors() : mutation.samples().stream().map(x -> OutputAnchor.create()).toList(),
                mutation.createdSampleAnchors() != null ? mutation.createdSampleAnchors() : mutation.samples().stream().map(x -> OutputSampleAnchor.create()).toList()
        );
    }

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ImportSDF mutation, ExperimentMutationContext context) {
        Iterator<OutputAnchor> anchorIt = mutation.createdOutputAnchors().iterator();
        Iterator<OutputSampleAnchor> sampleAnchorIt = mutation.createdSampleAnchors().iterator();
        Iterator<UUID> compoundIt = mutation.compoundIDs().iterator();
        for (SampleDTO sample : mutation.samples()) {
            CompoundEntity compound = compoundService.getCompound(compoundIt.next());
            ReactionOutput output = reaction.findOutput(compound.getId());
            if (output == null) {
                CompoundRef compound1 = compoundService.compoundRef(compound);
                OutputAnchor anchor = anchorIt.next();
                output = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, false, reaction.generateNextProductName(), anchor, compound1, EnteredValue.DEFAULT_ONE);
            }
            ReactionOutputSample.create(output, experiment.getName(), sampleAnchorIt.next(), SampleSource.VIRTUAL, null, EnteredValue.DEFAULT_ONE_HUNDRED);
            // TODO fill sample properties
        }
        context.getResponse().getMessages().add(mutation.samples().size() + " samples imported from SDF");
        return "Import SDF (" + mutation.samples().size() + " samples)";
    }
}
