package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ReactionOutputSampleMutation extends Mutation {

    OutputSampleAnchor anchor();

    record SetOutputDensity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double density,
            @Nullable DensityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double molarity,
            @Nullable MolarityUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double volume,
            @Nullable VolumeUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double purity,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHealthHazards (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<DictionaryItemRef> healthHazards
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double actualMol,
            @Nullable MolUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double actualWeight,
            @Nullable WeightUnit unit,
            @Nullable EnteredValueSource source
    ) implements ReactionOutputSampleMutation {
    }

    record RegisterSample (
            @NotNull OutputSampleAnchor anchor
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHandlingPrecautions (
            @NotNull OutputSampleAnchor anchor,
            @Nullable @Size(min = 1) List<DictionaryItemRef> handlingPrecautions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStorageInstructions (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<DictionaryItemRef> storageInstructions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputCompoundProtection (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<DictionaryItemRef> compoundProtection
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSolubilityInSolvents (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<SolubidityInSolvent> solubilityInSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputResidualSolvents (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<ResidualSolvent> residualSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMeltingPoint (
            @NotNull OutputSampleAnchor anchor,
            @Nullable MeltingPoint meltingPoint
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurityCalculations (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<PurityCalculation> purityCalculations
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputExternalSupplier (
            @NotNull OutputSampleAnchor anchor,
            @Nullable ExternalSupplier externalSupplier
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSource (
            @NotNull OutputSampleAnchor anchor,
            @Nullable DictionaryItemRef source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSourceDetails (
            @NotNull OutputSampleAnchor anchor,
            @Nullable DictionaryItemRef sourceDetails
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputComponentState (
            @NotNull OutputSampleAnchor anchor,
            @Nullable DictionaryItemRef componentState
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputBatchComment (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String batchComment
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStructureComment (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String structureComment
    ) implements ReactionOutputSampleMutation {
    }

    record RemoveProductSample (
            @NotNull OutputSampleAnchor anchor
    ) implements ReactionOutputSampleMutation {
    }
}
