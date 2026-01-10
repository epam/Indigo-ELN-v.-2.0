package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface ReactionOutputSampleMutation extends Mutation permits
        ReactionOutputSampleMutation.SetOutputDensity,
        ReactionOutputSampleMutation.SetOutputMolarity,
        ReactionOutputSampleMutation.SetOutputVolume,
        ReactionOutputSampleMutation.SetOutputPurity,
        ReactionOutputSampleMutation.SetOutputHealthHazards,
        ReactionOutputSampleMutation.SetOutputActualMol,
        ReactionOutputSampleMutation.SetOutputActualWeight,
        ReactionOutputSampleMutation.RegisterSample,
        ReactionOutputSampleMutation.SetOutputHandlingPrecautions,
        ReactionOutputSampleMutation.SetOutputStorageInstructions,
        ReactionOutputSampleMutation.SetOutputCompoundProtection,
        ReactionOutputSampleMutation.SetOutputSolubilityInSolvents,
        ReactionOutputSampleMutation.SetOutputResidualSolvents,
        ReactionOutputSampleMutation.SetOutputMeltingPoint,
        ReactionOutputSampleMutation.SetOutputPurityCalculations,
        ReactionOutputSampleMutation.SetOutputExternalSupplier,
        ReactionOutputSampleMutation.SetOutputSource,
        ReactionOutputSampleMutation.SetOutputSourceDetails,
        ReactionOutputSampleMutation.SetOutputComponentState,
        ReactionOutputSampleMutation.SetOutputBatchComment,
        ReactionOutputSampleMutation.SetOutputStructureComment,
        ReactionOutputSampleMutation.RemoveProductSample
{

    Anchor.OutputSample anchor();

    record SetOutputDensity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double purity,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHealthHazards (
            @NotNull Anchor.OutputSample anchor,
            @NotNull List<DictionaryItemRef> healthHazards
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double actualMol,
            @Nullable MolUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            @NotNull Anchor.OutputSample anchor,
            @Nullable Double actualWeight,
            @Nullable WeightUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record RegisterSample (
            @NotNull Anchor.OutputSample anchor
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHandlingPrecautions (
            @NotNull Anchor.OutputSample anchor,
            @Nullable @Size(min = 1) List<DictionaryItemRef> handlingPrecautions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStorageInstructions (
            @NotNull Anchor.OutputSample anchor,
            @Nullable List<DictionaryItemRef> storageInstructions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputCompoundProtection (
            @NotNull Anchor.OutputSample anchor,
            @Nullable List<DictionaryItemRef> compoundProtection
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSolubilityInSolvents (
            @NotNull Anchor.OutputSample anchor,
            @Nullable List<SolubidityInSolvent> solubilityInSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputResidualSolvents (
            @NotNull Anchor.OutputSample anchor,
            @Nullable List<ResidualSolvent> residualSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMeltingPoint (
            @NotNull Anchor.OutputSample anchor,
            @Nullable MeltingPoint meltingPoint
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurityCalculations (
            @NotNull Anchor.OutputSample anchor,
            @Nullable List<PurityCalculation> purityCalculations
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputExternalSupplier (
            @NotNull Anchor.OutputSample anchor,
            @Nullable ExternalSupplier externalSupplier
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSource (
            @NotNull Anchor.OutputSample anchor,
            @Nullable DictionaryItemRef source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSourceDetails (
            @NotNull Anchor.OutputSample anchor,
            @Nullable DictionaryItemRef sourceDetails
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputComponentState (
            @NotNull Anchor.OutputSample anchor,
            @Nullable DictionaryItemRef componentState
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputBatchComment (
            @NotNull Anchor.OutputSample anchor,
            @Nullable String batchComment
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStructureComment (
            @NotNull Anchor.OutputSample anchor,
            @Nullable String structureComment
    ) implements ReactionOutputSampleMutation {
    }

    record RemoveProductSample (
            @NotNull Anchor.OutputSample anchor
    ) implements ReactionOutputSampleMutation {
    }
}
