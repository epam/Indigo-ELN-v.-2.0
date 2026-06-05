package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.AccessLevel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionOutputSample extends ReactionSample<ReactionOutput> {

    @NotNull
    private OutputSampleAnchor anchor;

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

    @Nullable
    @Size(min = 1)
    private List<HandlingPrecautionsRef> handlingPrecautions;

    @Nullable
    @Size(min = 1)
    private List<StorageInstructionsRef> storageInstructions;

    @Nullable
    @Size(min = 1)
    private List<CompoundProtectionRef> compoundProtection;

    @Nullable
    @Size(min = 1)
    private List<@Valid SolubidityInSolvent> solubilityInSolvents;

    @Nullable
    @Size(min = 1)
    private List<@Valid ResidualSolvent> residualSolvents;

    @Valid
    @Nullable
    private MeltingPoint meltingPoint;

    @Nullable
    @Size(min = 1)
    private List<@Valid PurityCalculation> purityCalculations;

    @Valid
    @Nullable
    private ExternalSupplier externalSupplier;

    @Nullable
    private SampleSourceRef source;

    @Nullable
    private SampleSourceDetailsRef sourceDetails;

    @Nullable
    private ComponentStateRef componentState;

    @Nullable
    private String batchComment;

    @Nullable
    private String structureComment;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getShortNbkBatchNumber() {
        return nbkBatchNumber.getShortForm();
    }

    public static ReactionOutputSample create(ReactionOutput row, String experimentName, OutputSampleAnchor anchor, EnteredValue<NoUnit> purity) {
        ReactionOutputSample sample = new ReactionOutputSample();
        sample.row = row;
        sample.anchor = anchor;
        sample.nbkBatchNumber = new NbkBatchNumber(experimentName, row.getReaction().getModel().generateNextNbkBatchNumber());
        sample.purity = purity;
        row.getSamples().add(sample);
        return sample;
    }

    @Override
    protected List<? extends AbstractExperimentNode<ReactionOutput>> internalGetSiblings(ReactionOutput parent) {
        return parent.getSamples();
    }
}
