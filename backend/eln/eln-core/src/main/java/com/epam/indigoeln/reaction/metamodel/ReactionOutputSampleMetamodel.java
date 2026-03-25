package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.patch.EnteredValuePatch;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler2.EnteredValueDiffHandler;
import com.epam.indigoeln.reaction.model.units.*;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.enteredValueProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionOutputSampleMetamodel {

    // ReactionSample
    public static final ModelProperty<ReactionOutputSample, EnteredValue<DensityUnit>, ReactionOutputSamplePatch, EnteredValuePatch<DensityUnit>> DENSITY = enteredValueProperty("density", ReactionOutputSample::getDensity, ReactionOutputSample::setDensity, ReactionOutputSamplePatch::getDensity, ReactionOutputSamplePatch::setDensity);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<MolarityUnit>, ReactionOutputSamplePatch, EnteredValuePatch<MolarityUnit>> MOLARITY = enteredValueProperty("molarity", ReactionOutputSample::getMolarity, ReactionOutputSample::setMolarity, ReactionOutputSamplePatch::getMolarity, ReactionOutputSamplePatch::setMolarity);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<VolumeUnit>, ReactionOutputSamplePatch, EnteredValuePatch<VolumeUnit>> VOLUME = enteredValueProperty("volume", ReactionOutputSample::getVolume, ReactionOutputSample::setVolume, ReactionOutputSamplePatch::getVolume, ReactionOutputSamplePatch::setVolume);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<NoUnit>, ReactionOutputSamplePatch, EnteredValuePatch<NoUnit>> PURITY = enteredValueProperty("purity", ReactionOutputSample::getPurity, ReactionOutputSample::setPurity, ReactionOutputSamplePatch::getPurity, ReactionOutputSamplePatch::setPurity, new EnteredValueDiffHandler<>(null, EnteredValue.DEFAULT_ONE_HUNDRED));
    public static final ModelProperty<ReactionOutputSample, STRCodeSample, ReactionOutputSamplePatch, STRCodeSample> STR_CODE = property("strCode", ReactionOutputSample::getStrCode, ReactionOutputSample::setStrCode, ReactionOutputSamplePatch::getStrCode, ReactionOutputSamplePatch::setStrCode);
    public static final ModelProperty<ReactionOutputSample, List<DictionaryItemRef>, ReactionOutputSamplePatch, List<DictionaryItemRef>> HEALTH_HAZARDS = property("healthHazards", ReactionOutputSample::getHealthHazards, ReactionOutputSample::setHealthHazards, ReactionOutputSamplePatch::getHealthHazards, ReactionOutputSamplePatch::setHealthHazards);
    // ReactionInputSample
    public static final ModelProperty<ReactionOutputSample, OutputSampleAnchor, ReactionOutputSamplePatch, OutputSampleAnchor> ANCHOR = property("anchor", ReactionOutputSample::getAnchor, ReactionOutputSample::setAnchor, ReactionOutputSamplePatch::getAnchor, ReactionOutputSamplePatch::setAnchor);
    public static final ModelProperty<ReactionOutputSample, NbkBatchNumber, ReactionOutputSamplePatch, NbkBatchNumber> NBK_BATCH_NUMBER = property("nbkBatchNumber", ReactionOutputSample::getNbkBatchNumber, ReactionOutputSample::setNbkBatchNumber, ReactionOutputSamplePatch::getNbkBatchNumber, ReactionOutputSamplePatch::setNbkBatchNumber);
    public static final ModelProperty<ReactionOutputSample, String, ReactionOutputSamplePatch, String> SHORT_NBK_BATCH_NUMBER = property("shortNbkBatchNumber", ReactionOutputSample::getShortNbkBatchNumber, null, ReactionOutputSamplePatch::getShortNbkBatchNumber, ReactionOutputSamplePatch::setShortNbkBatchNumber);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<MolUnit>, ReactionOutputSamplePatch, EnteredValuePatch<MolUnit>> ACTUAL_MOL = enteredValueProperty("actualMol", ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol, ReactionOutputSamplePatch::getActualMol, ReactionOutputSamplePatch::setActualMol);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<WeightUnit>, ReactionOutputSamplePatch, EnteredValuePatch<WeightUnit>> ACTUAL_WEIGHT = enteredValueProperty("actualWeight", ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight, ReactionOutputSamplePatch::getActualWeight, ReactionOutputSamplePatch::setActualWeight);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<NoUnit>, ReactionOutputSamplePatch, EnteredValuePatch<NoUnit>> YIELD = enteredValueProperty("yield", ReactionOutputSample::getYield, ReactionOutputSample::setYield, ReactionOutputSamplePatch::getYield, ReactionOutputSamplePatch::setYield);
    public static final ModelProperty<ReactionOutputSample, SampleRegistrationStatus, ReactionOutputSamplePatch, SampleRegistrationStatus> REGISTRATION_STATUS = property("registrationStatus", ReactionOutputSample::getRegistrationStatus, ReactionOutputSample::setRegistrationStatus, ReactionOutputSamplePatch::getRegistrationStatus, ReactionOutputSamplePatch::setRegistrationStatus);
    public static final ModelProperty<ReactionOutputSample, String, ReactionOutputSamplePatch, String> REGISTRATION_STATUS_MESSAGE = property("registrationStatusMessage", ReactionOutputSample::getRegistrationStatusMessage, ReactionOutputSample::setRegistrationStatusMessage, ReactionOutputSamplePatch::getRegistrationStatusMessage, ReactionOutputSamplePatch::setRegistrationStatusMessage);
    public static final ModelProperty<ReactionOutputSample, UUID, ReactionOutputSamplePatch, UUID> SAMPLE_ID = property("sampleId", ReactionOutputSample::getSampleId, ReactionOutputSample::setSampleId, ReactionOutputSamplePatch::getSampleId, ReactionOutputSamplePatch::setSampleId);
    public static final ModelProperty<ReactionOutputSample, List<DictionaryItemRef>, ReactionOutputSamplePatch, List<DictionaryItemRef>> HANDLING_PRECAUTIONS = property("handlingPrecautions", ReactionOutputSample::getHandlingPrecautions, ReactionOutputSample::setHandlingPrecautions, ReactionOutputSamplePatch::getHandlingPrecautions, ReactionOutputSamplePatch::setHandlingPrecautions);
    public static final ModelProperty<ReactionOutputSample, List<DictionaryItemRef>, ReactionOutputSamplePatch, List<DictionaryItemRef>> STORAGE_INSTRUCTIONS = property("storageInstructions", ReactionOutputSample::getStorageInstructions, ReactionOutputSample::setStorageInstructions, ReactionOutputSamplePatch::getStorageInstructions, ReactionOutputSamplePatch::setStorageInstructions);
    public static final ModelProperty<ReactionOutputSample, List<DictionaryItemRef>, ReactionOutputSamplePatch, List<DictionaryItemRef>> COMPOUND_PROTECTION = property("compoundProtection", ReactionOutputSample::getCompoundProtection, ReactionOutputSample::setCompoundProtection, ReactionOutputSamplePatch::getCompoundProtection, ReactionOutputSamplePatch::setCompoundProtection);
    public static final ModelProperty<ReactionOutputSample, List<SolubidityInSolvent>, ReactionOutputSamplePatch, List<SolubidityInSolvent>> SOLUBILITY_IN_SOLVENTS = property("solubilityInSolvents", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents, ReactionOutputSamplePatch::getSolubilityInSolvents, ReactionOutputSamplePatch::setSolubilityInSolvents);
    public static final ModelProperty<ReactionOutputSample, List<ResidualSolvent>, ReactionOutputSamplePatch, List<ResidualSolvent>> RESIDUAL_SOLVENTS = property("residualSolvents", ReactionOutputSample::getResidualSolvents, ReactionOutputSample::setResidualSolvents, ReactionOutputSamplePatch::getResidualSolvents, ReactionOutputSamplePatch::setResidualSolvents);
    public static final ModelProperty<ReactionOutputSample, MeltingPoint, ReactionOutputSamplePatch, MeltingPoint> MELTING_POINT = property("meltingPoint", ReactionOutputSample::getMeltingPoint, ReactionOutputSample::setMeltingPoint, ReactionOutputSamplePatch::getMeltingPoint, ReactionOutputSamplePatch::setMeltingPoint);
    public static final ModelProperty<ReactionOutputSample, List<PurityCalculation>, ReactionOutputSamplePatch, List<PurityCalculation>> PURITY_CALCULATIONS = property("purityCalculations", ReactionOutputSample::getPurityCalculations, ReactionOutputSample::setPurityCalculations, ReactionOutputSamplePatch::getPurityCalculations, ReactionOutputSamplePatch::setPurityCalculations);
    public static final ModelProperty<ReactionOutputSample, ExternalSupplier, ReactionOutputSamplePatch, ExternalSupplier> EXTERNAL_SUPPLIER = property("externalSupplier", ReactionOutputSample::getExternalSupplier, ReactionOutputSample::setExternalSupplier, ReactionOutputSamplePatch::getExternalSupplier, ReactionOutputSamplePatch::setExternalSupplier);
    public static final ModelProperty<ReactionOutputSample, DictionaryItemRef, ReactionOutputSamplePatch, DictionaryItemRef> SOURCE = property("source", ReactionOutputSample::getSource, ReactionOutputSample::setSource, ReactionOutputSamplePatch::getSource, ReactionOutputSamplePatch::setSource);
    public static final ModelProperty<ReactionOutputSample, DictionaryItemRef, ReactionOutputSamplePatch, DictionaryItemRef> SOURCE_DETAILS = property("sourceDetails", ReactionOutputSample::getSourceDetails, ReactionOutputSample::setSourceDetails, ReactionOutputSamplePatch::getSourceDetails, ReactionOutputSamplePatch::setSourceDetails);
    public static final ModelProperty<ReactionOutputSample, DictionaryItemRef, ReactionOutputSamplePatch, DictionaryItemRef> COMPONENT_STATE = property("componentState", ReactionOutputSample::getComponentState, ReactionOutputSample::setComponentState, ReactionOutputSamplePatch::getComponentState, ReactionOutputSamplePatch::setComponentState);
    public static final ModelProperty<ReactionOutputSample, String, ReactionOutputSamplePatch, String> BATCH_COMMENT = property("batchComment", ReactionOutputSample::getBatchComment, ReactionOutputSample::setBatchComment, ReactionOutputSamplePatch::getBatchComment, ReactionOutputSamplePatch::setBatchComment);
    public static final ModelProperty<ReactionOutputSample, String, ReactionOutputSamplePatch, String> STRUCTURE_COMMENT = property("structureComment", ReactionOutputSample::getStructureComment, ReactionOutputSample::setStructureComment, ReactionOutputSamplePatch::getStructureComment, ReactionOutputSamplePatch::setStructureComment);
    
    public static final Metamodel<ReactionOutputSample, ReactionOutputSamplePatch> INSTANCE = new Metamodel<>("ReactionOutputSample", List.of(
            DENSITY,
            MOLARITY,
            VOLUME,
            PURITY,
            STR_CODE,
            HEALTH_HAZARDS,
            ANCHOR,
            NBK_BATCH_NUMBER,
            SHORT_NBK_BATCH_NUMBER,
            ACTUAL_MOL,
            ACTUAL_WEIGHT,
            YIELD,
            REGISTRATION_STATUS,
            REGISTRATION_STATUS_MESSAGE,
            SAMPLE_ID,
            HANDLING_PRECAUTIONS,
            STORAGE_INSTRUCTIONS,
            COMPOUND_PROTECTION,
            SOLUBILITY_IN_SOLVENTS,
            RESIDUAL_SOLVENTS,
            MELTING_POINT,
            PURITY_CALCULATIONS,
            EXTERNAL_SUPPLIER,
            SOURCE,
            SOURCE_DETAILS,
            COMPONENT_STATE,
            BATCH_COMMENT,
            STRUCTURE_COMMENT
    ));
}
