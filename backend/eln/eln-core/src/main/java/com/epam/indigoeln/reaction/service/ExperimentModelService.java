package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.ExperimentSnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.ExperimentModelUtil;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ExperimentDiffHandler;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
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
    ExperimentSnapshotMapper experimentSnapshotMapper;
    @Inject
    Validator validator;

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model);
        model.setReactions(List.of(reaction));
        model.setSchemaVersion(ExperimentModel.SCHEMA_VERSION);
        return model;
    }

    public ExperimentPatch applyMutation(ExperimentEntity experiment, Mutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);

        MutationHandler<Mutation, MutationRedoInfo> handler = mutationHandlerRegistry.findHandler(mutation);
        MutationContext context = new MutationContext(false, false, false);
        handler.initContext(experiment, mutation, context);

        ExperimentSnapshot initial = experimentSnapshotMapper.createSnapshot(experiment, context, true);
        ExperimentModel model = null;
        Set<Pair<ReactionRole, CompoundRef>> previousCompoundRefs = null;
        Map<Anchor.Reaction, @Nullable String> previousRxnFiles = null;
        if (context.isAffectsModel()) {
            model = experiment.getModel();
            previousCompoundRefs = ExperimentModelUtil.collectCompoundRefs(model);
            previousRxnFiles = StreamEx.of(model.getReactions())
                    .mapToEntry(Reaction::getAnchor, Reaction::getRxnfile)
                    .nonNullValues()
                    .toMap();
            ExperimentModelUtil.prepareToRecalculate(model);
        }

        MutationResult result = handler.handle(experiment, mutation, null, context);

        if (model != null) {
            validateModel(model);
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
            experiment.setModel(model);
            Set<ConstraintViolation<ExperimentModel>> violations = validator.validate(model);
            if (!violations.isEmpty()) {
                log.error("Mutation {} produced invalid model:\n{}", mutation, StreamEx.of(violations).joining("\n"));
                throw new RuntimeException("Mutation produced invalid model:\n" + StreamEx.of(violations).joining("\n"));
            }
        }

        updateDates(experiment, userService.getCurrentUserEntity());

        ExperimentSnapshot target = experimentSnapshotMapper.createSnapshot(experiment, context, false);
        ExperimentPatch diff = createPatch(initial, target, context);

        addRevision(experiment, experiment.getModifiedAt(), result.summary(), mutation, result.redoInfo(), result.reverseMutation(), diff);

        return diff;
    }

    private void validateModel(ExperimentModel model) {
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
            Preconditions.checkState(anchor <= model.getLastUsedAnchor());
        });
    }

    private void addRevision(ExperimentEntity experiment, ZonedDateTime datetime, String summary, Mutation mutation, @Nullable MutationRedoInfo redoInfo, @Nullable Mutation reverseMutation, ExperimentPatch diff) {
        ExperimentRevisionEntity revision = new ExperimentRevisionEntity();
        int newRevisionNo = experiment.getRevision() + 1;
        revision.setId(new ExperimentRevisionEntity.ExperimentRevisionID(experiment.getId(), newRevisionNo));
        revision.setExperiment(experiment);
        revision.setRevision(newRevisionNo);
        revision.setUser(userService.getCurrentUserEntity());
        revision.setDatetime(datetime);
        revision.setSummary(summary);
        revision.setMutation(mutation);
        revision.setRedoInfo(redoInfo);
        revision.setReverseMutation(reverseMutation);
        revision.setDiff(diff);
        experiment.setRevision(newRevisionNo);
        experiment.getRevisions().add(revision);
        experimentRepository.persistRevision(revision);
    }

    ExperimentPatch createPatch(ExperimentSnapshot a, ExperimentSnapshot b, MutationContext context) {
        ExperimentDiffHandler valueHandler = new ExperimentDiffHandler(context);
        //noinspection DataFlowIssue
        return valueHandler.compare(a, b).value();
    }
}
