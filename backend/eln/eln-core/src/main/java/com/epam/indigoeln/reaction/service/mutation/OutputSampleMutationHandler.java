package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.Dependent;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@Dependent
public class OutputSampleMutationHandler extends AbstractMutationHandler {

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputDensity mutation) {
        sample.setDensity(userLastEntered(mutation.density(), mutation.unit()));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolarity mutation) {
        sample.setMolarity(userLastEntered(mutation.molarity(), mutation.unit()));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputVolume mutation) {
        sample.setVolume(userLastEntered(mutation.volume(), mutation.unit()));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurity mutation) {
        sample.setPurity(userLastEntered(mutation.purity(), NoUnit.NO_UNIT, 1.0));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation) {
        sample.setHealthHazards(mutation.healthHazards());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation) {
        sample.setActualMol(userLastEntered(mutation.actualMol(), mutation.unit()));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation) {
        sample.setActualWeight(userLastEntered(mutation.actualWeight(), mutation.unit()));
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation) {
        sample.setHandlingPrecautions(mutation.handlingPrecautions());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation) {
        sample.setStorageInstructions(mutation.storageInstructions());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation) {
        sample.setCompoundProtection(mutation.compoundProtection());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation) {
        sample.setSolubilityInSolvents(mutation.solubilityInSolvents());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation) {
        sample.setResidualSolvents(mutation.residualSolvents());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation) {
        sample.setMeltingPoint(mutation.meltingPoint());
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation) {
        sample.setPurityCalculations(mutation.purityCalculations());
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation) {
        sample.setExternalSupplier(mutation.externalSupplier());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation) {
        sample.setSource(mutation.source());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation) {
        sample.setSourceDetails(mutation.sourceDetails());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation) {
        sample.setComponentState(mutation.componentState());
        dictionariesAffected = true;
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation) {
        sample.setBatchComment(mutation.batchComment());
    }

    public void handle(ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation) {
        sample.setStructureComment(mutation.structureComment());
    }
}
