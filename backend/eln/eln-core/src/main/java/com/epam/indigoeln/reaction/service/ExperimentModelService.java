package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.*;
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
            case ReactionMutation.SetScheme m -> setSchemeHandler.handle(experiment, model, m);
            case ReactionMutation.ResolveInputs m -> resolveInputsHandler.handle(experiment, model, m);
            case ReactionMutation.AddEmptyInput m -> setSchemeHandler.handle(experiment, model, m);
            case ReactionMutation.RemoveInput m -> setSchemeHandler.handle(experiment, model, m);

            case ReactionInputMutation.SetInputRole m -> inputMutationHandler.handle(experiment, model, m);
            case ReactionInputMutation.SetLimiting m -> inputMutationHandler.handle(model, m);
            case ReactionInputMutation.SetInputSaltCode m -> saltCodeEQHandler.handle(model, m);
            case ReactionInputMutation.SetInputSaltEQ m -> saltCodeEQHandler.handle(model, m);
            case ReactionInputMutation.SetInputEQ m -> inputMutationHandler.handle(model, m);

            case ReactionInputSampleMutation.SetInputDensity m -> inputSampleMutationHandler.handle(model, m);
            case ReactionInputSampleMutation.SetInputMolarity m -> inputSampleMutationHandler.handle(model, m);
            case ReactionInputSampleMutation.SetInputVolume m -> inputSampleMutationHandler.handle(model, m);
            case ReactionInputSampleMutation.SetInputPurity m -> inputSampleMutationHandler.handle(model, m);
            case ReactionInputSampleMutation.SetInputMol m -> inputSampleMutationHandler.handle(model, m);
            case ReactionInputSampleMutation.SetInputWeight m -> inputSampleMutationHandler.handle(model, m);

            case ReactionOutputMutation.AddProductSample m -> outputMutationHandler.handle(model, m);
            case ReactionOutputMutation.SetOutputType m -> outputMutationHandler.handle(model, m);
            case ReactionOutputMutation.SetOutputSaltCode m -> saltCodeEQHandler.handle(model, m);
            case ReactionOutputMutation.SetOutputSaltEQ m -> saltCodeEQHandler.handle(model, m);
            case ReactionOutputMutation.SetOutputEQ m -> outputMutationHandler.handle(model, m);

            case ReactionOutputSampleMutation.SetOutputDensity m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.SetOutputMolarity m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.SetOutputVolume m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.SetOutputPurity m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.SetOutputActualMol m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.SetOutputActualWeight m -> outputSampleMutationHandler.handle(model, m);
            case ReactionOutputSampleMutation.RegisterSample m -> registerSampleHandler.handle(model, m);
        }
        reactionCalculator.recalculate(model);
        return model;
    }
}
