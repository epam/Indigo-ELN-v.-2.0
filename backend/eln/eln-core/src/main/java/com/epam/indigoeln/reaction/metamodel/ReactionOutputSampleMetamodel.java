package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.outputsample.ExternalSupplier;
import com.epam.indigoeln.reaction.model.outputsample.MeltingPoint;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

class ReactionOutputSampleMetamodel {

    public static final Metamodel<ReactionOutputSample, ReactionOutputSamplePatch> INSTANCE = Metamodels.createMetamodel("ReactionOutputSample", m -> {
        m.property("anchor", ReactionOutputSample::getAnchor, ReactionOutputSample::setAnchor, ReactionOutputSamplePatch::getAnchor, ReactionOutputSamplePatch::setAnchor);
        m.accept(Metamodels::buildReactionSampleMetamodel);
        m.property("nbkBatchNumber", ReactionOutputSample::getNbkBatchNumber, ReactionOutputSample::setNbkBatchNumber, ReactionOutputSamplePatch::getNbkBatchNumber, ReactionOutputSamplePatch::setNbkBatchNumber);
        m.property("shortNbkBatchNumber", ReactionOutputSample::getShortNbkBatchNumber, null, ReactionOutputSamplePatch::getShortNbkBatchNumber, ReactionOutputSamplePatch::setShortNbkBatchNumber);
        m.enteredValueProperty("actualMol", ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol, ReactionOutputSamplePatch::getActualMol, ReactionOutputSamplePatch::setActualMol);
        m.enteredValueProperty("actualWeight", ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight, ReactionOutputSamplePatch::getActualWeight, ReactionOutputSamplePatch::setActualWeight);
        m.enteredValueProperty("yield", ReactionOutputSample::getYield, ReactionOutputSample::setYield, ReactionOutputSamplePatch::getYield, ReactionOutputSamplePatch::setYield);
        m.<@Nullable SampleRegistrationStatus>property("registrationStatus", ReactionOutputSample::getRegistrationStatus, ReactionOutputSample::setRegistrationStatus, ReactionOutputSamplePatch::getRegistrationStatus, ReactionOutputSamplePatch::setRegistrationStatus);
        m.<@Nullable String>property("registrationStatusMessage", ReactionOutputSample::getRegistrationStatusMessage, ReactionOutputSample::setRegistrationStatusMessage, ReactionOutputSamplePatch::getRegistrationStatusMessage, ReactionOutputSamplePatch::setRegistrationStatusMessage);
        m.<@Nullable UUID>property("sampleId", ReactionOutputSample::getSampleId, ReactionOutputSample::setSampleId, ReactionOutputSamplePatch::getSampleId, ReactionOutputSamplePatch::setSampleId);
        m.property("handlingPrecautions", ReactionOutputSample::getHandlingPrecautions, ReactionOutputSample::setHandlingPrecautions, ReactionOutputSamplePatch::getHandlingPrecautions, ReactionOutputSamplePatch::setHandlingPrecautions);
        m.property("storageInstructions", ReactionOutputSample::getStorageInstructions, ReactionOutputSample::setStorageInstructions, ReactionOutputSamplePatch::getStorageInstructions, ReactionOutputSamplePatch::setStorageInstructions);
        m.property("compoundProtection", ReactionOutputSample::getCompoundProtection, ReactionOutputSample::setCompoundProtection, ReactionOutputSamplePatch::getCompoundProtection, ReactionOutputSamplePatch::setCompoundProtection);
        m.property("solubilityInSolvents", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents, ReactionOutputSamplePatch::getSolubilityInSolvents, ReactionOutputSamplePatch::setSolubilityInSolvents);
        m.property("residualSolvents", ReactionOutputSample::getResidualSolvents, ReactionOutputSample::setResidualSolvents, ReactionOutputSamplePatch::getResidualSolvents, ReactionOutputSamplePatch::setResidualSolvents);
        m.<@Nullable MeltingPoint>property("meltingPoint", ReactionOutputSample::getMeltingPoint, ReactionOutputSample::setMeltingPoint, ReactionOutputSamplePatch::getMeltingPoint, ReactionOutputSamplePatch::setMeltingPoint);
        m.property("purityCalculations", ReactionOutputSample::getPurityCalculations, ReactionOutputSample::setPurityCalculations, ReactionOutputSamplePatch::getPurityCalculations, ReactionOutputSamplePatch::setPurityCalculations);
        m.<@Nullable ExternalSupplier>property("externalSupplier", ReactionOutputSample::getExternalSupplier, ReactionOutputSample::setExternalSupplier, ReactionOutputSamplePatch::getExternalSupplier, ReactionOutputSamplePatch::setExternalSupplier);
        m.property("source", ReactionOutputSample::getSource, ReactionOutputSample::setSource, ReactionOutputSamplePatch::getSource, ReactionOutputSamplePatch::setSource);
        m.property("sourceDetails", ReactionOutputSample::getSourceDetails, ReactionOutputSample::setSourceDetails, ReactionOutputSamplePatch::getSourceDetails, ReactionOutputSamplePatch::setSourceDetails);
        m.property("componentState", ReactionOutputSample::getComponentState, ReactionOutputSample::setComponentState, ReactionOutputSamplePatch::getComponentState, ReactionOutputSamplePatch::setComponentState);
        m.<@Nullable String>property("batchComment", ReactionOutputSample::getBatchComment, ReactionOutputSample::setBatchComment, ReactionOutputSamplePatch::getBatchComment, ReactionOutputSamplePatch::setBatchComment);
        m.<@Nullable String>property("structureComment", ReactionOutputSample::getStructureComment, ReactionOutputSample::setStructureComment, ReactionOutputSamplePatch::getStructureComment, ReactionOutputSamplePatch::setStructureComment);
    });
}
