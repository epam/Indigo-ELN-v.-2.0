package com.epam.indigoeln.reaction.service.mutation;

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
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
public abstract class AbstractMutationHandler<T extends Mutation, R extends MutationRedoInfo> implements ExperimentMutationHandler<T, R> {

    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;
    @Inject
    ReactionCalculator reactionCalculator;
    @Inject
    IndigoAPI indigoAPI;
    @Inject
    Validator validator;
    @Inject
    UserService userService;
    @Inject
    RevisionService revisionService;
    @Inject
    ExperimentRepository experimentRepository;

    @SuppressWarnings("DataFlowIssue")
    private Set<Pair<ReactionRole, CompoundRef>> previousCompoundRefs = null;
    @SuppressWarnings("DataFlowIssue")
    private Map<ReactionAnchor, @Nullable String> previousRxnFiles = null;

    protected boolean isAffectsAttachments() {
        return false;
    }

    protected boolean isAffectsACL() {
        return false;
    }

    protected boolean isAffectsModel() {
        return false;
    }

    public Pair<ExperimentSnapshot, ExperimentPatch> applyMutation(ExperimentEntity experiment, T mutation) {
        MutationContext context = new MutationContext();

        ExperimentSnapshot snapshotBefore = doSnapshotBefore(experiment);

        ExperimentModel model = isAffectsModel() ? experimentModelService.getModel(experiment) : null;
        if (model != null) {
            ExperimentModelUtil.prepareToRecalculate(model);
        }
        Integer revisionNo = experiment.getRevision() + 1;
        experiment.setRevision(revisionNo);
        MutationResult result = handle(experiment, model, mutation, null, context);

        if (model != null) {
            doRecalculateModel(experiment, model, context);
            experimentModelService.setModel(experiment, model);
            doValidateModel(experiment, model);
        }

        updateDates(experiment, userService.getCurrentUserEntity());
        if (experiment.getId() == null) {
            experimentRepository.persist(experiment);
        }

        ExperimentSnapshot snapshotAfter = doSnapshotAfter(experiment, model);
        ExperimentPatch diff = experimentModelService.createPatch(snapshotBefore, snapshotAfter, context);

        revisionService.addRevision(experiment, revisionNo, experiment.getModifiedAt(), result.summary(), mutation, result.redoInfo(), result.reverseMutation(), diff);

        return Pair.of(snapshotAfter, diff);
    }

    protected ExperimentSnapshot doSnapshotBefore(ExperimentEntity experiment) {
        ExperimentModel model = isAffectsModel() ? experimentModelService.getModel(experiment) : null;
        ExperimentSnapshot snapshot = snapshotMapper.createSnapshot(experiment, isAffectsAttachments(), isAffectsACL(), model);
        if (model != null) {
            previousCompoundRefs = ExperimentModelUtil.collectCompoundRefs(model);
            previousRxnFiles = StreamEx.of(model.getReactions())
                    .mapToEntry(Reaction::getAnchor, Reaction::getRxnfile)
                    .nonNullValues()
                    .toMap();
        }
        return snapshot;
    }

    protected ExperimentSnapshot doSnapshotAfter(ExperimentEntity experiment, @Nullable ExperimentModel model) {
        return snapshotMapper.createSnapshot(experiment, isAffectsAttachments(), isAffectsACL(), model);
    }

    protected void doRecalculateModel(ExperimentEntity experiment, ExperimentModel model, MutationContext context) {
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
    }

    protected void doValidateModel(ExperimentEntity experiment, ExperimentModel model) {
        Set<ConstraintViolation<ExperimentModel>> violations = validator.validate(model);
        if (!violations.isEmpty()) {
            log.error("Mutation produced invalid model:\n{}", StreamEx.of(violations).joining("\n"));
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
            throw new RuntimeException("Mutation produced invalid model: " + e.getMessage(), e);
        }
    }
}
