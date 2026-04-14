package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
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
    IndigoAPI indigoAPI;
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
                mutation.createdReactantAnchors() != null ? mutation.createdReactantAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> new InputAnchor(UUID.randomUUID())).toList(),
                mutation.createdReactantSampleAnchors() != null ? mutation.createdReactantSampleAnchors() : IntStreamEx.range(reactantCount).mapToObj(x -> new InputSampleAnchor(UUID.randomUUID())).toList(),
                mutation.createdCatalystAnchors() != null ? mutation.createdCatalystAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> new InputAnchor(UUID.randomUUID())).toList(),
                mutation.createdCatalystSampleAnchors() != null ? mutation.createdCatalystSampleAnchors() : IntStreamEx.range(catalystCount).mapToObj(x -> new InputSampleAnchor(UUID.randomUUID())).toList(),
                mutation.createdProductAnchors() != null ? mutation.createdProductAnchors() : IntStreamEx.range(productCount).mapToObj(x -> new OutputAnchor(UUID.randomUUID())).toList()
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.SetScheme mutation, ExperimentMutationContext context) {
        Map<Pair<String, Integer>, MoleculeLink<ReactionInput>> reactantLinks = new HashMap<>(), catalystLinks = new HashMap<>();
        Map<Pair<String, Integer>, MoleculeLink<ReactionOutput>> productLinks = new HashMap<>();
        if (reaction.getRxnfile() != null) {
            IndigoReaction indigoReaction = indigoAPI.loadReaction(reaction.getRxnfile());
            collectMoleculeLinks(indigoReaction.reactants(), reactantLinks, false);
            collectMoleculeLinks(indigoReaction.catalysts(), catalystLinks, false);
            for (ReactionInput row : reaction.getInputs()) {
                switch (row.getRole()) {
                    case REACTANT -> updateMoleculeLinkRow(reactantLinks, row);
                    case CATALYST -> updateMoleculeLinkRow(catalystLinks, row);
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
        createRows(catalystLinks, link -> createInputLine(reaction, link.molecule, ReactionRole.CATALYST, createdCatalystAnchors.next(), createdCatalystSampleAnchors.next()));
        createRows(productLinks, link -> createOutputLine(reaction, link.molecule, true, createdProductAnchors.next()));

        reaction.getInputs().sort(INPUT_COMPARATOR);
        reaction.getOutputs().sort(RXN_POSITION_COMPARATOR);

        adjustLimitingInput(reaction);
        reaction.setRxnfile(mutation.rxnFile());

        context.getResponse().setUnresolvedInputs(experimentService.analyzeRXN(reaction));

        return new MutationResult("Update reaction scheme");
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
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : new InputAnchor(UUID.randomUUID()),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : new InputSampleAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddEmptyInput mutation, ExperimentMutationContext context) {
        createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        adjustLimitingInput(reaction);
        return new MutationResult("Add empty input");
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.AddInput.class)
class AddInputHandler extends AbstractReactionMutationHandler<ReactionMutation.AddInput> {

    @Inject
    CompoundService compoundService;

    @Override
    protected ReactionMutation.AddInput doPrepareMutation(ExperimentEntity entity, ReactionMutation.AddInput mutation, ExperimentMutationContext context) {
        return new ReactionMutation.AddInput(
                mutation.anchor(),
                mutation.sampleId(),
                mutation.createdInputAnchor() != null ? mutation.createdInputAnchor() : new InputAnchor(UUID.randomUUID()),
                mutation.createdSampleAnchor() != null ? mutation.createdSampleAnchor() : new InputSampleAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.AddInput mutation, ExperimentMutationContext context) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT, checkNotNull(mutation.createdInputAnchor()), checkNotNull(mutation.createdSampleAnchor()));
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        setInputLineSample(row, sample, mutation.createdSampleAnchor(), context);
        adjustLimitingInput(reaction);

        return new MutationResult("Add input sample: " + getSampleIdentifier(sample));
    }
}

@Dependent
@MutationHandlerFor(ReactionMutation.ResolveInputs.class)
class ResolveInputsHandler extends AbstractReactionMutationHandler<ReactionMutation.ResolveInputs> {

    @Inject
    CompoundService compoundService;

    @Override
    protected ReactionMutation.ResolveInputs doPrepareMutation(ExperimentEntity entity, ReactionMutation.ResolveInputs mutation, ExperimentMutationContext context) {
        return new ReactionMutation.ResolveInputs(
                mutation.anchor(),
                mutation.inputSamples(),
                mutation.createdSampleAnchors() != null ? mutation.createdSampleAnchors() : EntryStream.of(mutation.inputSamples())
                        .mapValues(k -> new InputSampleAnchor(UUID.randomUUID()))
                        .toMap()
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation, ExperimentMutationContext context) {
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            setInputLineSample(row, sample, checkNotNull(mutation.createdSampleAnchors()).get(inputAnchor), context);
        });
        return new MutationResult("Resolve input samples");
    }
}
