package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    CompoundHandler compoundHandler;
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
                    case ReactionInputMutation.SetInputRowRole m -> inputMutationHandler.handle(experiment, model, input, m);
                    case ReactionInputMutation.SetInputRowMol m -> inputMutationHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputRowLimiting m -> inputMutationHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputRowSaltCode m -> compoundHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputRowSaltEQ m -> compoundHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputRowEQ m -> inputMutationHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputCompoundFormula m -> compoundHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputCompoundMolWeight m -> compoundHandler.handle(model, input, m);
                    case ReactionInputMutation.SetInputCompoundStereoisomerCode m -> compoundHandler.handle(model, input, m);
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
                    case ReactionOutputMutation.AddProductSample m -> outputMutationHandler.handle(experiment, model, output, m);
                    case ReactionOutputMutation.SetOutputRowType m -> outputMutationHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputRowSaltCode m -> compoundHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputRowSaltEQ m -> compoundHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputRowEQ m -> outputMutationHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputRowName m -> outputMutationHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputCompoundFormula m -> compoundHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputCompoundMolWeight m -> compoundHandler.handle(model, output, m);
                    case ReactionOutputMutation.SetOutputCompoundStereoisomerCode m -> compoundHandler.handle(model, output, m);
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
                    case ReactionOutputSampleMutation.RegisterSample m -> registerSampleHandler.handle(experiment, model, sample, m);

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
