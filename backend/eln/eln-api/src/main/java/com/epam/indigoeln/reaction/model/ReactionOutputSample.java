package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.AccessLevel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.appendToList;
import static com.epam.indigoeln.common.util.ModelUtil.removeFromList;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionOutputSample extends ReactionSample<ReactionOutput> {

    @NotNull
    private final OutputSampleAnchor anchor;

    @NotNull
    private final NbkBatchNumber nbkBatchNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<MolUnit> actualMol = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<WeightUnit> actualWeight = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("yield")
    private EnteredValue<NoUnit> yieldValue = EnteredValue.empty();

    @Nullable
    private SampleRegistrationStatus registrationStatus;

    @Nullable
    private String registrationStatusMessage;

    @Nullable
    private UUID sampleId;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<HandlingPrecautionsRef> handlingPrecautions = List.of();

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<StorageInstructionsRef> storageInstructions = List.of();

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CompoundProtectionRef> compoundProtection = List.of();

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<@Valid SolubidityInSolvent> solubilityInSolvents = List.of();

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<@Valid ResidualSolvent> residualSolvents = List.of();

    @Valid
    @Nullable
    private MeltingPoint meltingPoint;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<@Valid PurityCalculation> purityCalculations = List.of();

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
        return create(row, new NbkBatchNumber(experimentName, row.getReaction().getModel().generateNextNbkBatchNumber()), anchor, purity);
    }

    public static ReactionOutputSample create(ReactionOutput row, NbkBatchNumber nbkBatchNumber, OutputSampleAnchor anchor, EnteredValue<NoUnit> purity) {
        ReactionOutputSample sample = new ReactionOutputSample(anchor, nbkBatchNumber);
        sample.purity = purity;
        sample.insertInto(row);
        return sample;
    }

    @Override
    public void insertInto(ReactionOutput newParent) {
        //noinspection ConstantValue,DataFlowIssue
        Preconditions.checkState(row == null);
        row = newParent;
        row.setSamples(appendToList(row.getSamples(), this));
    }

    @Override
    public void delete() {
        //noinspection ConstantValue
        Preconditions.checkState(row != null);
        row.setSamples(removeFromList(row.getSamples(), this));
        //noinspection DataFlowIssue
        row = null;
    }
}
