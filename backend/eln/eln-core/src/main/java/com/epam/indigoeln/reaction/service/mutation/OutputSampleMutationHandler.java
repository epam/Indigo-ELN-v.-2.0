package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.outputsample.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@ApplicationScoped
public class OutputSampleMutationHandler extends AbstractMutationHandler {

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation) {
        sample.setDensity(userLastEntered(mutation.density(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation) {
        sample.setMolarity(userLastEntered(mutation.molarity(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation) {
        sample.setVolume(userLastEntered(mutation.volume(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation) {
        sample.setPurity(userLastEntered(mutation.purity(), NoUnit.NO_UNIT, 1.0));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation) {
        sample.setHealthHazards(mutation.healthHazards());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation) {
        sample.setActualMol(userLastEntered(mutation.actualMol(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation) {
        sample.setActualWeight(userLastEntered(mutation.actualWeight(), mutation.unit()));
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation) {
        sample.setHandlingPrecautions(mutation.handlingPrecautions());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation) {
        sample.setStorageInstructions(mutation.storageInstructions());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation) {
        sample.setCompoundProtection(mutation.compoundProtection());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation) {
        sample.setSolubilityInSolvents(mutation.solubilityInSolvents());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation) {
        sample.setResidualSolvents(mutation.residualSolvents());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation) {
        sample.setMeltingPoint(mutation.meltingPoint());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation) {
        sample.setPurityCalculations(mutation.purityCalculations());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation) {
        sample.setExternalSupplier(mutation.externalSupplier());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation) {
        sample.setSource(mutation.source());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation) {
        sample.setSourceDetails(mutation.sourceDetails());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation) {
        sample.setComponentState(mutation.componentState());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation) {
        sample.setBatchComment(mutation.batchComment());
    }

    public void handle(ExperimentModel model, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation) {
        sample.setStructureComment(mutation.structureComment());
    }
}
