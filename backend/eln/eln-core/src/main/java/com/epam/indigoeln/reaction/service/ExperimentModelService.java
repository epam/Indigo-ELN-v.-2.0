package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    ExperimentModelPatchService experimentModelPatchService;

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model);
        model.setReactions(List.of(reaction));
        model.setRevision(0);
        model.setSchemaVersion(ExperimentModel.SCHEMA_VERSION);
        return model;
    }

    @Valid
    public Pair<ExperimentModel, ExperimentModelPatch> applyMutation(ExperimentEntity experiment, ExperimentModel model, Mutation mutation) {
        log.debug("Mutating model for experiment {} with mutation {}", experiment.getId(), mutation);

        ExperimentModel initial = cloneModel(model);
        Set<DictionaryItemRef> previousDictionaryRefs = model.collectDictionaryRefs();
        Set<Pair<ReactionRole, CompoundRef>> previousCompoundRefs = model.collectCompoundRefs();
        Map<Anchor.Reaction, String> previousRxnFiles = StreamEx.of(model.getReactions()).toMap(Reaction::getAnchor, Reaction::getRxnfile);
        model.prepareToRecalculate();

        MutationHandler<?> handler = mutationHandlerRegistry.findHandler(mutation);
        MutationContext context = new MutationContext();
        MutationResult result = switch (mutation) {
            case ReactionMutation m -> {
                Reaction reaction = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionMutationHandler) handler).handle(experiment, model, reaction, m, context);
                yield null;
            }
            case ReactionInputMutation m -> {
                ReactionInput row = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionInputMutationHandler) handler).handle(experiment, model, row.getReaction(), row, m, context);
                yield null;
            }
            case ReactionInputSampleMutation m -> {
                ReactionInputSample sample = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionInputSampleMutationHandler) handler).handle(experiment, model, sample.getRow().getReaction(), sample.getRow(), sample, m, context);
                yield null;
            }
            case ReactionOutputMutation m -> {
                ReactionOutput row = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionOutputMutationHandler) handler).handle(experiment, model, row.getReaction(), row, m, context);
                yield null;
            }
            case ReactionOutputSampleMutation m -> {
                ReactionOutputSample sample = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionOutputSampleMutationHandler) handler).handle(experiment, model, sample.getRow().getReaction(), sample.getRow(), sample, m, context);
                yield null;
            }
            case ExperimentMutation m -> {
                //noinspection rawtypes,unchecked
                yield ((ExperimentMutationHandler) handler).handle(experiment, m, context);
            }
        };

        model.setRevision(model.getRevision() + 1);
        reactionCalculator.recalculate(model);

        boolean anyRxnfileChanged = false;
        for (Reaction reaction : model.getReactions()) {
            if (!reaction.getRxnfile().equals(previousRxnFiles.get(reaction.getAnchor())) || !context.getAffectedRoles().isEmpty()) {
                anyRxnfileChanged = true;
                IndigoReaction indigoReaction = reaction.getRxnfile().isEmpty() ? indigoAPI.createReaction() : indigoAPI.loadReaction(reaction.getRxnfile());
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
                    .remove(String::isEmpty)
                    .toList();
            experiment.setRxnfiles(rxnFiles);
        }

        Set<Pair<ReactionRole, CompoundRef>> currentCompoundRefs = model.collectCompoundRefs();
        if (!previousCompoundRefs.equals(currentCompoundRefs)) {
            Set<ExperimentReferencedCompound> ids = StreamEx.of(currentCompoundRefs)
                    .filter(p -> p.b().getCompoundID() != null)
                    .map(p -> new ExperimentReferencedCompound(p.a(), p.b().getCompoundID()))
                    .toSet();
            experiment.setReferencedCompounds(ids);
        }
        Set<DictionaryItemRef> currentDictionaryRefs = model.collectDictionaryRefs();
        if (!previousDictionaryRefs.equals(currentDictionaryRefs)) {
            Set<UUID> ids = StreamEx.of(currentDictionaryRefs).map(DictionaryItemRef::getId).toSet();
            experiment.setReferencedDictionaryItemIDs(ids);
        }

        updateDates(experiment, userService.getCurrentUserEntity());
        if (result == null) {
            result = new MutationResult("!!!");
        }
        experiment.setModel(model);

        ExperimentModelPatch diff = experimentModelPatchService.createPatch(initial, model);

        addRevision(experiment, experiment.getModifiedAt(), result.summary(), mutation, diff);

        return Pair.of(model, diff);
    }

    private ExperimentModel cloneModel(ExperimentModel model) {
        // TODO use clone? or restore from ProtoBuf?
        try {
            byte[] initialBytes = objectMapper.writeValueAsBytes(model);
            return objectMapper.readValue(initialBytes, ExperimentModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone model: " + e.getMessage(), e);
        }
    }

    private void addRevision(ExperimentEntity experiment, ZonedDateTime datetime, String summary, Mutation mutation, ExperimentModelPatch diff) {
        ExperimentRevisionEntity revision = new ExperimentRevisionEntity();
        int newRevisionNo = experiment.getRevision() + 1;
        revision.setId(new ExperimentRevisionEntity.ExperimentRevisionID(experiment.getId(), newRevisionNo));
        revision.setExperiment(experiment);
        revision.setRevision(newRevisionNo);
        revision.setUser(userService.getCurrentUserEntity());
        revision.setDatetime(datetime);
        revision.setSummary(summary);
        revision.setMutation(mutation);
        revision.setDiff(diff);
        experiment.setRevision(newRevisionNo);
        experiment.getRevisions().add(revision);
        experimentRepository.persistRevision(revision);
    }
}
