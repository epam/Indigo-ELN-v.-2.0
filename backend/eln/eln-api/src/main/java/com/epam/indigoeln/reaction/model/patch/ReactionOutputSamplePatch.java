package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionOutputSamplePatch extends AbstractReactionSamplePatch<Anchor.OutputSample> {

    private Patched<Anchor.OutputSample, Anchor.OutputSample> anchor;

    @Nullable
    private Patched<NbkBatchNumber, NbkBatchNumber> nbkBatchNumber;

    @Nullable
    private Patched<EnteredValue<MolUnit>, EnteredValuePatch<MolUnit>> actualMol;

    @Nullable
    private Patched<EnteredValue<WeightUnit>, EnteredValuePatch<WeightUnit>> actualWeight;

    @Nullable
    private Patched<EnteredValue<NoUnit>, EnteredValuePatch<NoUnit>> yield;

    @Nullable
    private Patched<SampleRegistrationStatus, SampleRegistrationStatus> registrationStatus;

    @Nullable
    private Patched<String, String> registrationStatusMessage;

    @Nullable
    private Patched<UUID, UUID> sampleId;

    @Nullable
    private Patched<List<DictionaryItemRef>, List<DictionaryItemRef>> handlingPrecautions;

    @Nullable
    private Patched<List<DictionaryItemRef>, List<DictionaryItemRef>> storageInstructions;

    @Nullable
    private Patched<List<DictionaryItemRef>, List<DictionaryItemRef>> compoundProtection;

    @Nullable
    private Patched<List<SolubidityInSolvent>, List<SolubidityInSolvent>> solubilityInSolvents;

    @Nullable
    private Patched<List<ResidualSolvent>, List<ResidualSolvent>> residualSolvents;

    @Nullable
    private Patched<MeltingPoint, MeltingPoint> meltingPoint;

    @Nullable
    private Patched<List<PurityCalculation>, List<PurityCalculation>> purityCalculations;

    @Nullable
    private Patched<ExternalSupplier, ExternalSupplier> externalSupplier;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> source;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> sourceDetails;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> componentState;

    @Nullable
    private Patched<String, String> batchComment;

    @Nullable
    private Patched<String, String> structureComment;
}
