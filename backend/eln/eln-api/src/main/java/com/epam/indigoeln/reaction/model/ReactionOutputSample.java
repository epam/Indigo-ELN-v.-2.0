package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputSamplePatch;
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
import java.util.function.Consumer;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = "row")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutputSample extends ReactionSample implements ExperimentModelNode {

    public static final Metamodel<ReactionOutputSample, ReactionOutputSamplePatch> METAMODEL = new Metamodel<ReactionOutputSample, ReactionOutputSamplePatch>("ReactionOutputSample")
            .anchorProperty("anchor", ReactionOutputSample::getAnchor, ReactionOutputSample::setAnchor, ReactionOutputSamplePatch::getAnchor, ReactionOutputSamplePatch::setAnchor)
            .accept(ReactionSample::addBaseProperties)
            .simpleProperty("nbkBatchNumber", ReactionOutputSample::getNbkBatchNumber, ReactionOutputSample::setNbkBatchNumber, ReactionOutputSamplePatch::getNbkBatchNumber, ReactionOutputSamplePatch::setNbkBatchNumber)
            .enteredValueProperty("actualMol", ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol, ReactionOutputSamplePatch::getActualMol, ReactionOutputSamplePatch::setActualMol)
            .enteredValueProperty("actualWeight", ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight, ReactionOutputSamplePatch::getActualWeight, ReactionOutputSamplePatch::setActualWeight)
            .enteredValueProperty("yield", ReactionOutputSample::getYield, ReactionOutputSample::setYield, ReactionOutputSamplePatch::getYield, ReactionOutputSamplePatch::setYield)
            .simpleProperty("registrationStatus", ReactionOutputSample::getRegistrationStatus, ReactionOutputSample::setRegistrationStatus, ReactionOutputSamplePatch::getRegistrationStatus, ReactionOutputSamplePatch::setRegistrationStatus)
            .simpleProperty("registrationStatusMessage", ReactionOutputSample::getRegistrationStatusMessage, ReactionOutputSample::setRegistrationStatusMessage, ReactionOutputSamplePatch::getRegistrationStatusMessage, ReactionOutputSamplePatch::setRegistrationStatusMessage)
            .simpleProperty("sampleId", ReactionOutputSample::getSampleId, ReactionOutputSample::setSampleId, ReactionOutputSamplePatch::getSampleId, ReactionOutputSamplePatch::setSampleId)
            .dictionaryListProperty("handlingPrecautions", ReactionOutputSample::getHandlingPrecautions, ReactionOutputSample::setHandlingPrecautions, ReactionOutputSamplePatch::getHandlingPrecautions, ReactionOutputSamplePatch::setHandlingPrecautions)
            .dictionaryListProperty("storageInstructions", ReactionOutputSample::getStorageInstructions, ReactionOutputSample::setStorageInstructions, ReactionOutputSamplePatch::getStorageInstructions, ReactionOutputSamplePatch::setStorageInstructions)
            .dictionaryListProperty("compoundProtection", ReactionOutputSample::getCompoundProtection, ReactionOutputSample::setCompoundProtection, ReactionOutputSamplePatch::getCompoundProtection, ReactionOutputSamplePatch::setCompoundProtection)
            .simpleProperty("solubilityInSolvents", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents, ReactionOutputSamplePatch::getSolubilityInSolvents, ReactionOutputSamplePatch::setSolubilityInSolvents)
            .simpleProperty("residualSolvents", ReactionOutputSample::getResidualSolvents, ReactionOutputSample::setResidualSolvents, ReactionOutputSamplePatch::getResidualSolvents, ReactionOutputSamplePatch::setResidualSolvents)
            .simpleProperty("meltingPoint", ReactionOutputSample::getMeltingPoint, ReactionOutputSample::setMeltingPoint, ReactionOutputSamplePatch::getMeltingPoint, ReactionOutputSamplePatch::setMeltingPoint)
            .simpleProperty("purityCalculations", ReactionOutputSample::getPurityCalculations, ReactionOutputSample::setPurityCalculations, ReactionOutputSamplePatch::getPurityCalculations, ReactionOutputSamplePatch::setPurityCalculations)
            .simpleProperty("externalSupplier", ReactionOutputSample::getSolubilityInSolvents, ReactionOutputSample::setSolubilityInSolvents, ReactionOutputSamplePatch::getSolubilityInSolvents, ReactionOutputSamplePatch::setSolubilityInSolvents)
            .dictionaryProperty("source", ReactionOutputSample::getSource, ReactionOutputSample::setSource, ReactionOutputSamplePatch::getSource, ReactionOutputSamplePatch::setSource)
            .dictionaryProperty("sourceDetails", ReactionOutputSample::getSourceDetails, ReactionOutputSample::setSourceDetails, ReactionOutputSamplePatch::getSourceDetails, ReactionOutputSamplePatch::setSourceDetails)
            .dictionaryProperty("componentState", ReactionOutputSample::getComponentState, ReactionOutputSample::setComponentState, ReactionOutputSamplePatch::getComponentState, ReactionOutputSamplePatch::setComponentState)
            .simpleProperty("batchComment", ReactionOutputSample::getBatchComment, ReactionOutputSample::setBatchComment, ReactionOutputSamplePatch::getBatchComment, ReactionOutputSamplePatch::setBatchComment)
            .simpleProperty("structureComment", ReactionOutputSample::getStructureComment, ReactionOutputSample::setStructureComment, ReactionOutputSamplePatch::getStructureComment, ReactionOutputSamplePatch::setStructureComment)
            .simpleProperty("calculatedMolWeight", ReactionOutputSample::getCalculatedMolWeight, null, ReactionOutputSamplePatch::getCalculatedMolWeight, ReactionOutputSamplePatch::setCalculatedMolWeight)
            .simpleProperty("calculatedBatchMF", ReactionOutputSample::getCalculatedBatchMF, null, ReactionOutputSamplePatch::getCalculatedBatchMF, ReactionOutputSamplePatch::setCalculatedBatchMF)
            .simpleProperty("precursorReactantIds", ReactionOutputSample::getPrecursorReactantIds, null, ReactionOutputSamplePatch::getPrecursorReactantIds, ReactionOutputSamplePatch::setPrecursorReactantIds)
            ;

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
        sample.anchor = new Anchor.OutputSample(row.getReaction().getModel().generateNextAnchor());
        return sample;
    }

    @Override
    public void prepareToRecalculate() {
        super.prepareToRecalculate();
        EnteredValue.prepareToRecalculate(actualMol, this::setActualMol);
        EnteredValue.prepareToRecalculate(actualWeight, this::setActualWeight);
        EnteredValue.prepareToRecalculate(yield, this::setYield);
    }

    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Double getCalculatedMolWeight() {
        return row.getCompound().getMolWeight() != null ? row.getCompound().getMolWeight().getValue() : null;
    }

    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCalculatedBatchMF() {
        String parentFormula = row.getCompound().getFormula();
        if (parentFormula == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parentFormula);
        if (row.getCompound().getSaltCode() != null) {
            sb.append(" * ").append(row.getCompound().getSaltEQ()).append(" (").append(row.getCompound().getSaltCode().getFormula()).append(")");
        }
        return sb.toString();
    }

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<STRCodeCompound> getPrecursorReactantIds() {
        return StreamEx.of(row.getReaction().getInputs())
                .filter(r -> r.getRole() == ReactionRole.REACTANT)
                .map(r -> r.getCompound().getStrCode())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void collectDictionaries(Consumer<@Nullable DictionaryItemRef> consumer) {
        super.collectDictionaries(consumer);
        handlingPrecautions.forEach(consumer);
        storageInstructions.forEach(consumer);
        compoundProtection.forEach(consumer);
        solubilityInSolvents.stream().map(SolubidityInSolvent::getSolvent).forEach(consumer);
        residualSolvents.stream().map(ResidualSolvent::getSolvent).forEach(consumer);
        consumer.accept(externalSupplier != null ? externalSupplier.getSupplier() : null);
        consumer.accept(source);
        consumer.accept(sourceDetails);
        consumer.accept(componentState);
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }
}
