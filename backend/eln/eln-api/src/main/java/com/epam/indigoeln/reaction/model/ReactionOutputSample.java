package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = "row")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutputSample extends ReactionSample implements ExperimentModelNode {

    public static void buildMetamodel(Metamodel<ReactionOutputSample, ReactionOutputSamplePatch> metamodel) {
        metamodel.setName("ReactionOutputSample");
        metamodel.property("anchor", ReactionOutputSample::getAnchor, ReactionOutputSample::setAnchor, ReactionOutputSamplePatch::getAnchor, ReactionOutputSamplePatch::setAnchor);
        metamodel.accept(ReactionSample::buildMetamodelBase);
        metamodel.property("nbkBatchNumber", ReactionOutputSample::getNbkBatchNumber, ReactionOutputSample::setNbkBatchNumber, ReactionOutputSamplePatch::getNbkBatchNumber, ReactionOutputSamplePatch::setNbkBatchNumber);
        metamodel.enteredValueProperty("actualMol", ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol, ReactionOutputSamplePatch::getActualMol, ReactionOutputSamplePatch::setActualMol);
        metamodel.enteredValueProperty("actualWeight", ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight, ReactionOutputSamplePatch::getActualWeight, ReactionOutputSamplePatch::setActualWeight);
        metamodel.enteredValueProperty("yield", ReactionOutputSample::getYield, ReactionOutputSample::setYield, ReactionOutputSamplePatch::getYield, ReactionOutputSamplePatch::setYield);
        metamodel.<@Nullable SampleRegistrationStatus>property("registrationStatus", ReactionOutputSample::getRegistrationStatus, ReactionOutputSample::setRegistrationStatus, ReactionOutputSamplePatch::getRegistrationStatus, ReactionOutputSamplePatch::setRegistrationStatus);
        metamodel.<@Nullable String>property("registrationStatusMessage", ReactionOutputSample::getRegistrationStatusMessage, ReactionOutputSample::setRegistrationStatusMessage, ReactionOutputSamplePatch::getRegistrationStatusMessage, ReactionOutputSamplePatch::setRegistrationStatusMessage);
        metamodel.<@Nullable UUID>property("sampleId", ReactionOutputSample::getSampleId, ReactionOutputSample::setSampleId, ReactionOutputSamplePatch::getSampleId, ReactionOutputSamplePatch::setSampleId);
        metamodel.property("handlingPrecautions", ReactionOutputSample::getHandlingPrecautions, ReactionOutputSample::setHandlingPrecautions, ReactionOutputSamplePatch::getHandlingPrecautions, ReactionOutputSamplePatch::setHandlingPrecautions, null);
        metamodel.property("storageInstructions", ReactionOutputSample::getStorageInstructions, ReactionOutputSample::setStorageInstructions, ReactionOutputSamplePatch::getStorageInstructions, ReactionOutputSamplePatch::setStorageInstructions, null);
        metamodel.property("compoundProtection", ReactionOutputSample::getCompoundProtection, ReactionOutputSample::setCompoundProtection, ReactionOutputSamplePatch::getCompoundProtection, ReactionOutputSamplePatch::setCompoundProtection, null);
        metamodel.property("solubilityInSolvents", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents, ReactionOutputSamplePatch::getSolubilityInSolvents, ReactionOutputSamplePatch::setSolubilityInSolvents, null);
        metamodel.property("residualSolvents", ReactionOutputSample::getResidualSolvents, ReactionOutputSample::setResidualSolvents, ReactionOutputSamplePatch::getResidualSolvents, ReactionOutputSamplePatch::setResidualSolvents, null);
        metamodel.<@Nullable MeltingPoint>property("meltingPoint", ReactionOutputSample::getMeltingPoint, ReactionOutputSample::setMeltingPoint, ReactionOutputSamplePatch::getMeltingPoint, ReactionOutputSamplePatch::setMeltingPoint);
        metamodel.property("purityCalculations", ReactionOutputSample::getPurityCalculations, ReactionOutputSample::setPurityCalculations, ReactionOutputSamplePatch::getPurityCalculations, ReactionOutputSamplePatch::setPurityCalculations, null);
        metamodel.<@Nullable ExternalSupplier>property("externalSupplier", ReactionOutputSample::getExternalSupplier, ReactionOutputSample::setExternalSupplier, ReactionOutputSamplePatch::getExternalSupplier, ReactionOutputSamplePatch::setExternalSupplier);
        metamodel.property("source", ReactionOutputSample::getSource, ReactionOutputSample::setSource, ReactionOutputSamplePatch::getSource, ReactionOutputSamplePatch::setSource, null);
        metamodel.property("sourceDetails", ReactionOutputSample::getSourceDetails, ReactionOutputSample::setSourceDetails, ReactionOutputSamplePatch::getSourceDetails, ReactionOutputSamplePatch::setSourceDetails, null);
        metamodel.property("componentState", ReactionOutputSample::getComponentState, ReactionOutputSample::setComponentState, ReactionOutputSamplePatch::getComponentState, ReactionOutputSamplePatch::setComponentState, null);
        metamodel.<@Nullable String>property("batchComment", ReactionOutputSample::getBatchComment, ReactionOutputSample::setBatchComment, ReactionOutputSamplePatch::getBatchComment, ReactionOutputSamplePatch::setBatchComment);
        metamodel.<@Nullable String>property("structureComment", ReactionOutputSample::getStructureComment, ReactionOutputSample::setStructureComment, ReactionOutputSamplePatch::getStructureComment, ReactionOutputSamplePatch::setStructureComment);
        metamodel.<@Nullable Double>property("calculatedMolWeight", ReactionOutputSample::getCalculatedMolWeight, null, ReactionOutputSamplePatch::getCalculatedMolWeight, ReactionOutputSamplePatch::setCalculatedMolWeight);
        metamodel.property("precursorReactantIds", ReactionOutputSample::getPrecursorReactantIds, null, ReactionOutputSamplePatch::getPrecursorReactantIds, ReactionOutputSamplePatch::setPrecursorReactantIds, null);
    }

    @JsonBackReference
    private ReactionOutput row;

    @NotNull
    private Anchor.OutputSample anchor;

    @NotNull
    private NbkBatchNumber nbkBatchNumber;

    @Nullable
    private EnteredValue<MolUnit> actualMol;

    @Nullable
    private EnteredValue<WeightUnit> actualWeight;

    @Nullable
    private EnteredValue<NoUnit> yield;

    @Nullable
    private SampleRegistrationStatus registrationStatus;

    @Nullable
    private String registrationStatusMessage;

    @Nullable
    private UUID sampleId;

    @NotNull
    private List<DictionaryItemRef> handlingPrecautions = List.of();

    @NotNull
    private List<DictionaryItemRef> storageInstructions = List.of();

    @NotNull
    private List<DictionaryItemRef> compoundProtection = List.of();

    @NotNull
    private List<SolubidityInSolvent> solubilityInSolvents = List.of();

    @NotNull
    private List<ResidualSolvent> residualSolvents = List.of();

    @Nullable
    private MeltingPoint meltingPoint;

    @NotNull
    private List<PurityCalculation> purityCalculations = List.of();

    @Nullable
    private ExternalSupplier externalSupplier;

    @Nullable
    private DictionaryItemRef source;

    @Nullable
    private DictionaryItemRef sourceDetails;

    @Nullable
    private DictionaryItemRef componentState;

    @Nullable
    private String batchComment;

    @Nullable
    private String structureComment;

    public static ReactionOutputSample create(String experimentName, ReactionOutput row) {
        ReactionOutputSample sample = createWithAnchor(row, new Anchor.OutputSample(row.getReaction().getModel().generateNextAnchor()));
        sample.nbkBatchNumber = new NbkBatchNumber(experimentName, row.getReaction().getModel().generateNextNbkBatchNumber());
        return sample;
    }

    public static ReactionOutputSample createWithAnchor(ReactionOutput row, Anchor.OutputSample anchor) {
        ReactionOutputSample sample = new ReactionOutputSample();
        sample.row = row;
        sample.anchor = anchor;
        return sample;
    }

    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Double getCalculatedMolWeight() {
        return row.getCompound().getMolWeight() != null ? row.getCompound().getMolWeight().getValue() : null;
    }

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<STRCodeSample> getPrecursorReactantIds() {
        return StreamEx.of(row.getReaction().getInputs())
                .filter(r -> r.getRole() == ReactionRole.REACTANT)
                .flatMap(r -> r.getSamples().stream())
                .map(ReactionSample::getStrCode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(Handlers.OUTPUT_SAMPLE_METAMODEL, this);
    }
}
