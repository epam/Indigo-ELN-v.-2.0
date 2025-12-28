package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentReferencedCompound;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    public ExperimentModel applyMutation(ExperimentEntity experiment, ExperimentModel model, Mutation mutation) {
        Set<DictionaryItemRef> previousDictionaryRefs = model.collectDictionaryRefs();
        Set<Pair<ReactionRole, CompoundRef>> previousCompoundRefs = model.collectCompoundRefs();
        Map<Anchor.Reaction, String> previousRxnFiles = StreamEx.of(model.getReactions()).toMap(Reaction::getAnchor, Reaction::getRxnfile);
        model.prepareToRecalculate();

        MutationHandler<?> handler = mutationHandlerRegistry.findHandler(mutation);
        MutationContext context = new MutationContext();
        switch (mutation) {
            case ReactionMutation m -> {
                Reaction reaction = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionMutationHandler) handler).handle(experiment, model, reaction, m, context);
            }
            case ReactionInputMutation m -> {
                ReactionInput row = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionInputMutationHandler) handler).handle(experiment, model, row.getReaction(), row, m, context);
            }
            case ReactionInputSampleMutation m -> {
                ReactionInputSample sample = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionInputSampleMutationHandler) handler).handle(experiment, model, sample.getRow().getReaction(), sample.getRow(), sample, m, context);
            }
            case ReactionOutputMutation m -> {
                ReactionOutput row = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionOutputMutationHandler) handler).handle(experiment, model, row.getReaction(), row, m, context);
            }
            case ReactionOutputSampleMutation m -> {
                ReactionOutputSample sample = model.locate(m.anchor());
                //noinspection rawtypes,unchecked
                ((ReactionOutputSampleMutationHandler) handler).handle(experiment, model, sample.getRow().getReaction(), sample.getRow(), sample, m, context);
            }
        }

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
        return model;
    }
}
