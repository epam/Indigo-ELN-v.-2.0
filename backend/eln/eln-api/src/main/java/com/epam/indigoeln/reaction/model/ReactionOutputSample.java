package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.StreamUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "row")
@EqualsAndHashCode(callSuper = true, exclude = "row")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionOutputSample extends ReactionSample implements ExperimentNode {

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
                .collect(StreamUtil.toListNotNull());
    }
}
