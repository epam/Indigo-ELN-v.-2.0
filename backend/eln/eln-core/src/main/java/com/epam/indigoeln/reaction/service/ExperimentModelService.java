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
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelService {

    @Inject
    ReactionCalculator reactionCalculator;
    @Inject
    Provider<SchemaHandler> schemeHandler;
    @Inject
    Provider<InputMutationHandler> inputMutationHandler;
    @Inject
    Provider<CompoundHandler> compoundHandler;
    @Inject
    Provider<InputSampleMutationHandler> inputSampleMutationHandler;
    @Inject
    Provider<OutputMutationHandler> outputMutationHandler;
    @Inject
    Provider<OutputSampleMutationHandler> outputSampleMutationHandler;
    @Inject
    Provider<RegisterSampleHandler> registerSampleHandler;
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

        // don't rewrite to dynamic lookup to have compile-time guarantee that all mutations are handled
        Pair<AbstractMutationHandler, Runnable> pair = switch (mutation) {
            case ReactionMutation rm -> {
                Reaction reaction = model.locate(rm);
                yield switch (rm) {
                    case ReactionMutation.SetScheme m -> resolve(schemeHandler, h -> h.handle(reaction, m));
                    case ReactionMutation.ResolveInputs m -> resolve(schemeHandler, h -> h.handle(reaction, m));
                    case ReactionMutation.AddEmptyInput m -> resolve(schemeHandler, h -> h.handle(reaction, m));
                    case ReactionMutation.AddInput m -> resolve(schemeHandler, h -> h.handle(reaction, m));
                    case ReactionMutation.RemoveInput m -> resolve(schemeHandler, h -> h.handle(reaction, m));
                };
            }
            case ReactionInputMutation im -> {
                ReactionInput input = model.locate(im);
                yield switch (im) {
                    case ReactionInputMutation.SetInputRowRole m -> resolve(inputMutationHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowMol m -> resolve(inputMutationHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowChemicalName m -> resolve(inputMutationHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowLimiting m -> resolve(inputMutationHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowSaltCode m -> resolve(compoundHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowSaltEQ m -> resolve(compoundHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputRowEQ m -> resolve(inputMutationHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputCompoundMolWeight m -> resolve(compoundHandler, h -> h.handle(input, m));
                    case ReactionInputMutation.SetInputCompoundStereoisomerCode m -> resolve(compoundHandler, h -> h.handle(input, m));
                };
            }
            case ReactionInputSampleMutation ism -> {
                ReactionInputSample sample = model.locate(ism);
                yield switch (ism) {
                    case ReactionInputSampleMutation.SetInputDensity m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputMolarity m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputVolume m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputPurity m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputMol m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputWeight m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputHealthHazards m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionInputSampleMutation.SetInputComment m -> resolve(inputSampleMutationHandler, h -> h.handle(sample, m));
                };
            }
            case ReactionOutputMutation om -> {
                ReactionOutput output = model.locate(om);
                yield switch (om) {
                    case ReactionOutputMutation.AddProductSample m -> resolve(outputMutationHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputRowType m -> resolve(outputMutationHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputRowSaltCode m -> resolve(compoundHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputRowSaltEQ m -> resolve(compoundHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputRowEQ m -> resolve(outputMutationHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputRowName m -> resolve(outputMutationHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputCompoundMolWeight m -> resolve(compoundHandler, h -> h.handle(output, m));
                    case ReactionOutputMutation.SetOutputCompoundStereoisomerCode m -> resolve(compoundHandler, h -> h.handle(output, m));
                };
            }
            case ReactionOutputSampleMutation osm -> {
                ReactionOutputSample sample = model.locate(osm);
                yield switch (osm) {
                    case ReactionOutputSampleMutation.SetOutputDensity m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputMolarity m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputVolume m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputPurity m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputHealthHazards m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputActualMol m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputActualWeight m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.RegisterSample m -> resolve(registerSampleHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputHandlingPrecautions m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputStorageInstructions m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputCompoundProtection m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputSolubilityInSolvents m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputResidualSolvents m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputMeltingPoint m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputPurityCalculations m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputExternalSupplier m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputSource m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputSourceDetails m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputComponentState m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputBatchComment m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                    case ReactionOutputSampleMutation.SetOutputStructureComment m -> resolve(outputSampleMutationHandler, h -> h.handle(sample, m));
                };
            }
        };

        AbstractMutationHandler handler = pair.a();
        handler.setExperiment(experiment);
        handler.setModel(model);
        Runnable handlerRun = pair.b();
        handlerRun.run();
        model.setRevision(model.getRevision() + 1);
        reactionCalculator.recalculate(model);

        boolean anyRxnfileChanged = false;
        for (Reaction reaction : model.getReactions()) {
            if (!reaction.getRxnfile().equals(previousRxnFiles.get(reaction.getAnchor())) || !handler.getAffectedRoles().isEmpty()) {
                anyRxnfileChanged = true;
                IndigoReaction indigoReaction = reaction.getRxnfile().isEmpty() ? indigoAPI.createReaction() : indigoAPI.loadReaction(reaction.getRxnfile());
                if (!handler.getAffectedRoles().isEmpty()) {
                    experimentModelHelperService.rebuildReactionRxnFile(experiment, reaction, handler.getAffectedRoles(), indigoReaction);
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

    private <H extends AbstractMutationHandler> Pair<AbstractMutationHandler, Runnable> resolve(Provider<H> provider, Consumer<H> operation) {
        H handler = provider.get();
        return Pair.of(handler, () -> operation.accept(handler));
    }
}
