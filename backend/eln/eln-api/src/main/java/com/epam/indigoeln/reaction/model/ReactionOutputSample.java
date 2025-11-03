package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
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
public final class ReactionOutputSample extends ReactionSample implements ExperimentModelNode, ToStringTree {

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
    public void toStringTree(Builder builder) {
        builder.open("ReactionOutputSample")
                .property("anchor", anchor)
                .property("actualMol", actualMol)
                .property("actualWeight", actualWeight)
                .property("density", density)
                .property("molarity", molarity)
                .property("volume", volume)
                .property("purity", purity)
                .property("yield", yield)
                .property("registrationStatus", registrationStatus)
                .property("registrationStatusMessage", registrationStatusMessage)
                .property("sampleId", sampleId)
                .property("strCode", strCode)
                .property("nbkBatchNumber", nbkBatchNumber)
                .close();
    }
}
