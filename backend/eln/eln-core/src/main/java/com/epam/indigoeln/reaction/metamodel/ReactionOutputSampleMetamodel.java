package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.enteredValueProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ReactionOutputSampleMetamodel {

    // ReactionSample
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<DensityUnit>> DENSITY = enteredValueProperty("density", ReactionOutputSample::getDensity, ReactionOutputSample::setDensity);
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<MolarityUnit>> MOLARITY = enteredValueProperty("molarity", ReactionOutputSample::getMolarity, ReactionOutputSample::setMolarity);
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<VolumeUnit>> VOLUME = enteredValueProperty("volume", ReactionOutputSample::getVolume, ReactionOutputSample::setVolume);
    public static final ModelProperty<ReactionOutputSample, EnteredValue<NoUnit>> PURITY = enteredValueProperty("purity", ReactionOutputSample::getPurity, ReactionOutputSample::setPurity, EnteredValue.DEFAULT_ONE_HUNDRED);
    public static final ModelProperty<ReactionOutputSample, @Nullable STRCodeSample> STR_CODE = property("strCode", ReactionOutputSample::getStrCode, ReactionOutputSample::setStrCode);
    public static final ModelProperty<ReactionOutputSample, List<HealthHazardRef>> HEALTH_HAZARDS = property("healthHazards", ReactionOutputSample::getHealthHazards, ReactionOutputSample::setHealthHazards);
    // ReactionInputSample
    public static final ModelProperty<ReactionOutputSample, OutputSampleAnchor> ANCHOR = property("anchor", ReactionOutputSample::getAnchor, null);
    public static final ModelProperty<ReactionOutputSample, NbkBatchNumber> NBK_BATCH_NUMBER = property("nbkBatchNumber", ReactionOutputSample::getNbkBatchNumber, null);
    public static final ModelProperty<ReactionOutputSample, String> SHORT_NBK_BATCH_NUMBER = property("shortNbkBatchNumber", ReactionOutputSample::getShortNbkBatchNumber, null);
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<MolUnit>> ACTUAL_MOL = enteredValueProperty("actualMol", ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol);
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<WeightUnit>> ACTUAL_WEIGHT = enteredValueProperty("actualWeight", ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight);
    public static final ModelProperty<ReactionOutputSample, @Nullable EnteredValue<NoUnit>> YIELD = enteredValueProperty("yield", ReactionOutputSample::getYield, ReactionOutputSample::setYield);
    public static final ModelProperty<ReactionOutputSample, @Nullable SampleRegistrationStatus> REGISTRATION_STATUS = property("registrationStatus", ReactionOutputSample::getRegistrationStatus, ReactionOutputSample::setRegistrationStatus);
    public static final ModelProperty<ReactionOutputSample, @Nullable String> REGISTRATION_STATUS_MESSAGE = property("registrationStatusMessage", ReactionOutputSample::getRegistrationStatusMessage, ReactionOutputSample::setRegistrationStatusMessage);
    public static final ModelProperty<ReactionOutputSample, @Nullable UUID> SAMPLE_ID = property("sampleId", ReactionOutputSample::getSampleId, ReactionOutputSample::setSampleId);
    public static final ModelProperty<ReactionOutputSample, List<HandlingPrecautionsRef>> HANDLING_PRECAUTIONS = property("handlingPrecautions", ReactionOutputSample::getHandlingPrecautions, ReactionOutputSample::setHandlingPrecautions);
    public static final ModelProperty<ReactionOutputSample, List<StorageInstructionsRef>> STORAGE_INSTRUCTIONS = property("storageInstructions", ReactionOutputSample::getStorageInstructions, ReactionOutputSample::setStorageInstructions);
    public static final ModelProperty<ReactionOutputSample, List<CompoundProtectionRef>> COMPOUND_PROTECTION = property("compoundProtection", ReactionOutputSample::getCompoundProtection, ReactionOutputSample::setCompoundProtection);
    public static final ModelProperty<ReactionOutputSample, List<SolubidityInSolvent>> SOLUBILITY_IN_SOLVENTS = property("solubilityInSolvents", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents);
    public static final ModelProperty<ReactionOutputSample, List<ResidualSolvent>> RESIDUAL_SOLVENTS = property("residualSolvents", ReactionOutputSample::getResidualSolvents, ReactionOutputSample::setResidualSolvents);
    public static final ModelProperty<ReactionOutputSample, @Nullable MeltingPoint> MELTING_POINT = property("meltingPoint", ReactionOutputSample::getMeltingPoint, ReactionOutputSample::setMeltingPoint);
    public static final ModelProperty<ReactionOutputSample, List<PurityCalculation>> PURITY_CALCULATIONS = property("purityCalculations", ReactionOutputSample::getPurityCalculations, ReactionOutputSample::setPurityCalculations);
    public static final ModelProperty<ReactionOutputSample, @Nullable ExternalSupplier> EXTERNAL_SUPPLIER = property("externalSupplier", ReactionOutputSample::getExternalSupplier, ReactionOutputSample::setExternalSupplier);
    public static final ModelProperty<ReactionOutputSample, SampleSourceRef> SOURCE = property("source", ReactionOutputSample::getSource, ReactionOutputSample::setSource);
    public static final ModelProperty<ReactionOutputSample, @Nullable SampleSourceDetailsRef> SOURCE_DETAILS = property("sourceDetails", ReactionOutputSample::getSourceDetails, ReactionOutputSample::setSourceDetails);
    public static final ModelProperty<ReactionOutputSample, @Nullable ComponentStateRef> COMPONENT_STATE = property("componentState", ReactionOutputSample::getComponentState, ReactionOutputSample::setComponentState);
    public static final ModelProperty<ReactionOutputSample, @Nullable String> BATCH_COMMENT = property("batchComment", ReactionOutputSample::getBatchComment, ReactionOutputSample::setBatchComment);
    public static final ModelProperty<ReactionOutputSample, @Nullable String> STRUCTURE_COMMENT = property("structureComment", ReactionOutputSample::getStructureComment, ReactionOutputSample::setStructureComment);
    
    public static final Metamodel<ReactionOutputSample> INSTANCE = new Metamodel<>("ReactionOutputSample", List.of(
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
