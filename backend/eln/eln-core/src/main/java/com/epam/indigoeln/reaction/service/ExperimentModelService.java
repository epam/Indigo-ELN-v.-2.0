package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.ExperimentModelUtil;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ExperimentDiffHandler;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelService {

    @Inject
    ReactionCalculator reactionCalculator;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;
    @Inject
    IndigoAPI indigoAPI;
    @Inject
    UserService userService;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    Validator validator;
    @Inject
    RevisionService revisionService;

    ObjectReader modelReader;
    ObjectWriter modelWriter;
    ObjectReader patchReader;
    ObjectWriter patchWriter;

    ExperimentModelService(ObjectMapper objectMapper) {
        modelReader = objectMapper.readerFor(ExperimentModel.class);
        modelWriter = objectMapper.writerFor(ExperimentModel.class);
        patchReader = objectMapper.readerFor(ExperimentPatch.class);
        patchWriter = objectMapper.writerFor(ExperimentPatch.class);
    }

    @Valid
    public ExperimentModel createNewModel(ExperimentEntity experiment) {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, experiment.generateNextAnchor(ReactionAnchor.class));
        model.setReactions(List.of(reaction));
        model.setSchemaVersion(ExperimentModel.SCHEMA_VERSION);
        return model;
    }

    @SneakyThrows // !!!
    public Pair<ExperimentModel, ExperimentPatch> applyMutation(ExperimentEntity experiment, Mutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);

        ExperimentMutationHandler<Mutation, MutationRedoInfo> handler = mutationHandlerRegistry.findHandler(mutation);
        MutationContext context = new MutationContext(false, false, false);
        handler.initContext(experiment, mutation, context);

        ExperimentSnapshot initial = snapshotMapper.createSnapshot(experiment, context, () -> getModel(experiment));
        ExperimentModel model = null;
        Set<Pair<ReactionRole, CompoundRef>> previousCompoundRefs = null;
        Map<ReactionAnchor, @Nullable String> previousRxnFiles = null;
        if (context.isAffectsModel()) {
            model = getModel(experiment);
            previousCompoundRefs = ExperimentModelUtil.collectCompoundRefs(model);
            previousRxnFiles = StreamEx.of(model.getReactions())
                    .mapToEntry(Reaction::getAnchor, Reaction::getRxnfile)
                    .nonNullValues()
                    .toMap();
            ExperimentModelUtil.prepareToRecalculate(model);
        }

        MutationResult result = handler.handle(experiment, model, mutation, null, context);

        if (model != null) {
            reactionCalculator.recalculate(model);

            boolean anyRxnfileChanged = false;
            for (Reaction reaction : model.getReactions()) {
                if (!Objects.equals(reaction.getRxnfile(), previousRxnFiles.get(reaction.getAnchor())) || !context.getAffectedRoles().isEmpty()) {
                    anyRxnfileChanged = true;
                    IndigoReaction indigoReaction = reaction.getRxnfile() == null ? indigoAPI.createReaction() : indigoAPI.loadReaction(reaction.getRxnfile());
                    if (!context.getAffectedRoles().isEmpty()) {
                        experimentModelHelperService.rebuildReactionRxnFile(experiment, reaction, context.getAffectedRoles(), indigoReaction);
                    }
                    experimentModelHelperService.rebuildReactionPicture(experiment, reaction, indigoReaction);
                    reaction.setRxnVersion(reaction.getRxnVersion() + 1);
                }
            }
            if (anyRxnfileChanged) {
                List<String> rxnFiles = StreamEx.of(model.getReactions())
                        .map(Reaction::getRxnfile)
                        .collect(StreamUtil.toListNotNull());
                experiment.setRxnfiles(rxnFiles);
            }

            Set<Pair<ReactionRole, CompoundRef>> currentCompoundRefs = ExperimentModelUtil.collectCompoundRefs(model);
            if (!previousCompoundRefs.equals(currentCompoundRefs)) {
                Set<ExperimentReferencedCompound> ids = StreamEx.of(currentCompoundRefs)
                        .filter(p -> p.b().getCompoundID() != null)
                        .map(p -> new ExperimentReferencedCompound(p.a(), p.b().getCompoundID()))
                        .toSet();
                experiment.setReferencedCompounds(ids);
            }

            setModel(experiment, model);
            validateModel(experiment, model, mutation);
        }

        updateDates(experiment, userService.getCurrentUserEntity());

        ExperimentSnapshot target = snapshotMapper.createSnapshot(experiment, context, () -> getModel(experiment));
        ExperimentPatch diff = createPatch(initial, target, context);

        revisionService.addRevision(experiment, experiment.getModifiedAt(), result.summary(), mutation, result.redoInfo(), result.reverseMutation(), diff);

        return Pair.of(model, diff);
    }

    private void validateModel(ExperimentEntity experiment, ExperimentModel model, Mutation mutation) {
        Set<ConstraintViolation<ExperimentModel>> violations = validator.validate(model);
        if (!violations.isEmpty()) {
            log.error("Mutation {} produced invalid model:\n{}", mutation, StreamEx.of(violations).joining("\n"));
            throw new RuntimeException("Mutation produced invalid model:\n" + StreamEx.of(violations).joining("\n"));
        }

        try {
            // check all parent links are correct
            for (Reaction reaction : model.getReactions()) {
                Preconditions.checkState(reaction.getModel() == model);
                for (ReactionInput input : reaction.getInputs()) {
                    Preconditions.checkState(input.getReaction() == reaction);
                    for (ReactionInputSample sample : input.getSamples()) {
                        Preconditions.checkState(sample.getRow() == input);
                    }
                }
                for (ReactionOutput output : reaction.getOutputs()) {
                    Preconditions.checkState(output.getReaction() == reaction);
                    for (ReactionOutputSample sample : output.getSamples()) {
                        Preconditions.checkState(sample.getRow() == output);
                    }
                }
            }
            // check anchors are unique
            Map<Integer, Integer> anchors = new HashMap<>();
            for (Reaction reaction : model.getReactions()) {
                anchors.merge(reaction.getAnchor().getNumber(), 1, Integer::sum);
                for (ReactionInput input : reaction.getInputs()) {
                    anchors.merge(input.getAnchor().getNumber(), 1, Integer::sum);
                    for (ReactionInputSample sample : input.getSamples()) {
                        anchors.merge(sample.getAnchor().getNumber(), 1, Integer::sum);
                    }
                }
                for (ReactionOutput output : reaction.getOutputs()) {
                    anchors.merge(output.getAnchor().getNumber(), 1, Integer::sum);
                    for (ReactionOutputSample sample : output.getSamples()) {
                        anchors.merge(sample.getAnchor().getNumber(), 1, Integer::sum);
                    }
                }
            }
            anchors.forEach((anchor, count) -> {
                Preconditions.checkState(count <= 1, "Anchor %s used multiple times", anchor);
                Preconditions.checkState(anchor <= experiment.getLastUsedAnchor());
            });
        } catch (Exception e) {
            throw new RuntimeException("Mutation " + mutation + " produced invalid model: " + e.getMessage(), e);
        }
    }

    ExperimentPatch createPatch(ExperimentSnapshot a, ExperimentSnapshot b, MutationContext context) {
        ExperimentDiffHandler valueHandler = new ExperimentDiffHandler(context);
        //noinspection DataFlowIssue
        return valueHandler.compare(a, b).updatedValue();
    }

    public ExperimentModel getModel(ExperimentEntity experiment) {
        try {
            return modelReader.readValue(experiment.getModel());
        } catch (Exception e) {
            throw new RuntimeException("Cannot read experiment model: " + e.getMessage(), e);
        }
    }

    public void setModel(ExperimentEntity experiment, ExperimentModel model) {
        try {
            experiment.setModel(modelWriter.writeValueAsString(model));
        } catch (Exception e) {
            throw new RuntimeException("Cannot write experiment model: " + e.getMessage(), e);
        }
    }
}
