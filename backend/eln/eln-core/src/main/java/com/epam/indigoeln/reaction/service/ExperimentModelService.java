package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelService {

    @Inject
    ReactionCalculator reactionCalculator;
    @Inject
    SetSchemeHandler setSchemeHandler;
    @Inject
    ResolveInputsHandler resolveInputsHandler;
    @Inject
    InputMutationHandler inputMutationHandler;
    @Inject
    SaltCodeEQHandler saltCodeEQHandler;
    @Inject
    InputSampleMutationHandler inputSampleMutationHandler;
    @Inject
    OutputMutationHandler outputMutationHandler;
    @Inject
    OutputSampleMutationHandler outputSampleMutationHandler;
    @Inject
    RegisterSampleHandler registerSampleHandler;

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model);
        model.setReactions(List.of(reaction));
        return model;
    }

    @Valid
    public ExperimentModel applyMutation(ExperimentEntity experiment, ExperimentModel model, Mutation mutation) {
        model.prepareToRecalculate();
        // don't rewrite to dynamic lookup to have compile-time guarantee that all mutations are handled
        switch (mutation) {
            case ReactionMutation rm -> {
                Reaction reaction = model.locate(rm);
                switch (rm) {
                    case ReactionMutation.SetScheme m -> setSchemeHandler.handle(experiment, model, reaction, m);
                    case ReactionMutation.ResolveInputs m -> resolveInputsHandler.handle(experiment, model, reaction, m);
                    case ReactionMutation.AddEmptyInput m -> setSchemeHandler.handle(experiment, model, reaction, m);
                    case ReactionMutation.RemoveInput m -> setSchemeHandler.handle(experiment, model, reaction, m);
                }
            }
            case ReactionInputMutation im -> {
                ReactionInput input = model.locate(im);
                switch (im) {
                    case ReactionInputMutation.SetInputRole m -> inputMutationHandler.handle(experiment, model, input, m);
                    case ReactionInputMutation.SetInputMol m -> inputMutationHandler.handle(model, input, m);
                    case ReactionInputMutation.SetLimiting m -> inputMutationHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputSaltCode m -> saltCodeEQHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputSaltEQ m -> saltCodeEQHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputEQ m -> inputMutationHandler.handle(model, input, m);
                }
            }
            case ReactionInputSampleMutation ism -> {
                ReactionInputSample sample = model.locate(ism);
                switch (ism) {
                    case ReactionInputSampleMutation.SetInputDensity m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputMolarity m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputVolume m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputPurity m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputMol m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputWeight m -> inputSampleMutationHandler.handle(model, sample, m);
                    case ReactionInputSampleMutation.SetInputHealthHazards m -> inputSampleMutationHandler.handle(model, sample, m);
                }
            }
            case ReactionOutputMutation om -> {
                ReactionOutput output = model.locate(om);
                switch (om) {
                    case ReactionOutputMutation.AddProductSample m -> outputMutationHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputType m -> outputMutationHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputSaltCode m -> saltCodeEQHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputSaltEQ m -> saltCodeEQHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputEQ m -> outputMutationHandler.handle(model, output, m);
                }
            }
            case ReactionOutputSampleMutation osm -> {
                ReactionOutputSample sample = model.locate(osm);
                switch (osm) {
                    case ReactionOutputSampleMutation.SetOutputDensity m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputMolarity m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputVolume m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputPurity m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputHealthHazards m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputActualMol m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputActualWeight m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.RegisterSample m -> registerSampleHandler.handle(model, sample, m);

                    case ReactionOutputSampleMutation.SetOutputHandlingPrecautions m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputStorageInstructions m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputCompoundProtection m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputSolubilityInSolvents m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputResidualSolvents m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputMeltingPoint m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputPurityCalculations m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputExternalSupplier m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputSource m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputSourceDetails m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputComponentState m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputBatchComment m -> outputSampleMutationHandler.handle(model, sample, m);
                    case ReactionOutputSampleMutation.SetOutputStructureComment m -> outputSampleMutationHandler.handle(model, sample, m);

                }
            }
        }
        reactionCalculator.recalculate(model);
        return model;
    }
}
