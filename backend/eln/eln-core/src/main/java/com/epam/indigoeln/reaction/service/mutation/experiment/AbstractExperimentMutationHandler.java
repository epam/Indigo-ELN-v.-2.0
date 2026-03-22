package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEditSessionEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.MutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.util.SignificantFiguresUtil;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public abstract class AbstractExperimentMutationHandler<T extends Mutation> extends MutationHandler<T, ExperimentEntity, ExperimentSnapshot, ExperimentRevisionEntity, ExperimentMutationContext> {

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
    @Inject
    protected ACLService aclService;

    @Override
    protected ExperimentMutationContext createContext() {
        return new ExperimentMutationContext();
    }

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, T mutation, ExperimentMutationContext context) {
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
    }

    protected final ExperimentSnapshot doSnapshotBefore(ExperimentEntity experiment, ExperimentMutationContext context) {
        if (context.isAffectsModel()) {
            experimentModelService.readModel(experiment);
        }
        ExperimentSnapshot snapshot = snapshotMapper.createSnapshot(experiment, context.isAffectsAttachments(), context.isAffectsACL(), experiment.getModelObj());
        if (context.isAffectsModel()) {
            // read another copy that will be updated during the mutation
            experimentModelService.readModel(experiment);
        }
        return snapshot;
    }

    @Override
    protected final JsonNode doUpdateEntity(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter, ExperimentMutationContext context) {
        ExperimentModel model = experiment.getModelObj();
        if (model != null) {
            SignificantFiguresUtil.setSignificantFigures(model.getSignificantFigures());
            doValidateModel(model);
            reactionCalculator.recalculate(model);
            doUpdateReferences(experiment, model,  snapshotBefore, snapshotAfter, context);
            doValidateModel(model);
            SignificantFiguresUtil.clearSignificantFigures();
        }
        updateDates(experiment, userService.getCurrentUserEntity());
        JsonNode patch = experimentModelService.createPatch(snapshotBefore, snapshotAfter);
        if (model != null) {
            experimentModelService.setModel(experiment, model);
        }
        //noinspection ConstantValue
        if (experiment.getId() == null) {
            experimentRepository.persist(experiment);
            experimentRepository.flushAndRefresh(experiment);
        }
        return patch;
    }

    @Override
    protected ExperimentSnapshot doSnapshotAfter(ExperimentEntity experiment, ExperimentMutationContext context) {
        return snapshotMapper.createSnapshot(experiment, context.isAffectsAttachments(), context.isAffectsACL(), experiment.getModelObj());
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, T mutation, MutationResult result, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = revisionService.addRevision(experiment, revisionNo, experiment.getModifiedAt(), result.summary(), mutation, patch);
        ExperimentEditSessionEntity editSession = experimentModelService.getEditSession(experiment, userService.getCurrentUserEntity());
        if (context.isRequiresEditSession()) {
            if (editSession == null) {
                editSession = experimentModelService.createEditSession(experiment, userService.getCurrentUserEntity(), revision.getDatetime());
            } else {
                editSession.setLastActive(revision.getDatetime());
            }
            revision.setEditSession(editSession);
        } else {
            if (editSession != null) {
                editSession.setFinished(editSession.getLastActive());
            }
        }
        return revision;
    }

    protected void doUpdateReferences(ExperimentEntity experiment, ExperimentModel model, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter, ExperimentMutationContext context) {
        boolean anyRxnfileChanged = false;
        Map<ReactionAnchor, @Nullable String> oldRxnFiles = checkNotNull(snapshotBefore.getRxnFiles());
        Map<ReactionAnchor, @Nullable String> newRxnFiles = checkNotNull(snapshotAfter.getRxnFiles());
        for (Reaction reaction : model.getReactions()) {
            if (!Objects.equals(oldRxnFiles.get(reaction.getAnchor()), newRxnFiles.get(reaction.getAnchor())) || context.isSchemaAffected()) {
                anyRxnfileChanged = true;
                IndigoReaction indigoReaction;
                if (context.isSchemaAffected()) {
                    indigoReaction = experimentModelHelperService.rebuildReactionRxnFile(reaction);
                } else {
                    indigoReaction = reaction.getRxnfile() == null ? indigoAPI.createReaction() : indigoAPI.loadReaction(reaction.getRxnfile());
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

        Set<Pair<ReactionRole, CompoundRef>> oldCompoundRefs = checkNotNull(snapshotBefore.getCompoundRefs());
        Set<Pair<ReactionRole, CompoundRef>> newCompoundRefs = checkNotNull(snapshotAfter.getCompoundRefs());
        if (!oldCompoundRefs.equals(newCompoundRefs)) {
            Set<ExperimentReferencedCompound> ids = StreamEx.of(newCompoundRefs)
                    .filter(p -> p.b().getCompoundID() != null)
                    .map(p -> new ExperimentReferencedCompound(p.a(), p.b().getCompoundID()))
                    .toSet();
            experiment.setReferencedCompounds(ids);
        }
    }

    protected void doValidateModel(ExperimentModel model) {
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
        } catch (Exception e) {
            throw new RuntimeException("Mutation produced invalid model: " + e.getMessage(), e);
        }
    }
}
