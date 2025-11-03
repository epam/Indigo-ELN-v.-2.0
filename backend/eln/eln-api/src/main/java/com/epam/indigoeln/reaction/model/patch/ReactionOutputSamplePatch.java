package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactionOutputSamplePatch extends AbstractReactionSamplePatch<Anchor.OutputSample> {

    @Nullable
    private Optional<NbkBatchNumber> nbkBatchNumber;

    @Nullable
    private Optional<EnteredValuePatch<MolUnit>> actualMol;

    @Nullable
    private Optional<EnteredValuePatch<WeightUnit>> actualWeight;

    @Nullable
    private Optional<EnteredValuePatch<NoUnit>> yield;

    @Nullable
    private Optional<SampleRegistrationStatus> registrationStatus;

    @Nullable
    private Optional<UUID> sampleId;

    @Nullable
    private Optional<List<DictionaryItemRef>> handlingPrecautions;

    @Nullable
    private Optional<List<DictionaryItemRef>> storageInstructions;

    @Nullable
    private Optional<List<DictionaryItemRef>> compoundProtection;

    // TODO all other properties
//    @Nullable
//    private Optional<List<SolubidityInSolvent>> solubidityInSolvents;
}
