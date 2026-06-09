package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ReactionOutputSampleMutation extends ExperimentMutation {

    OutputSampleAnchor anchor();

    record SetOutputDensity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String density,
            @Nullable DensityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMolarity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String molarity,
            @Nullable MolarityUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputVolume (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String volume,
            @Nullable VolumeUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurity (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String purity
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHealthHazards (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<HealthHazardRef> healthHazards
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualMol (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String actualMol,
            @Nullable MolUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputActualWeight (
            @NotNull OutputSampleAnchor anchor,
            @Nullable String actualWeight,
            @Nullable WeightUnit unit
    ) implements ReactionOutputSampleMutation {
    }

    record RegisterSample (
            @NotNull OutputSampleAnchor anchor
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputHandlingPrecautions (
            @NotNull OutputSampleAnchor anchor,
            @Nullable @Size(min = 1) List<HandlingPrecautionsRef> handlingPrecautions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStorageInstructions (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<StorageInstructionsRef> storageInstructions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputCompoundProtection (
            @NotNull OutputSampleAnchor anchor,
            @Nullable List<CompoundProtectionRef> compoundProtection
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
            @Nullable SampleSourceRef source
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSourceDetails (
            @NotNull OutputSampleAnchor anchor,
            @Nullable SampleSourceDetailsRef sourceDetails
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputComponentState (
            @NotNull OutputSampleAnchor anchor,
            @Nullable ComponentStateRef componentState
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

    record SetOutputSaltCode (
            @NotNull OutputSampleAnchor anchor,
            @Nullable SaltCodeRef saltCode,
            @Nullable OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputSaltCode(@NotNull OutputSampleAnchor anchor, @Nullable SaltCodeRef saltCode) {
            this(anchor, saltCode, null);
        }
    }

    record SetOutputSaltEQ (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double saltEQ,
            @Nullable OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputSaltEQ(@NotNull OutputSampleAnchor anchor, @Nullable Double saltEQ) {
            this(anchor, saltEQ, null);
        }
    }

    record SetOutputStereoisomerCode (
            @NotNull OutputSampleAnchor anchor,
            @Nullable StereoisomerCodeRef stereoisomerCode,
            @Nullable OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputStereoisomerCode(@NotNull OutputSampleAnchor anchor, @Nullable StereoisomerCodeRef stereoisomerCode) {
            this(anchor, stereoisomerCode, null);
        }
    }

    record SetOutputMolfile (
            @NotNull OutputSampleAnchor anchor,
            @NotNull String molfile,
            @Nullable OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputMolfile(@NotNull OutputSampleAnchor anchor, @NotNull String molfile) {
            this(anchor, molfile, null);
        }

        @Override
        public String toString() {
            return "SetOutputMolfile[" +
                    "anchor=" + anchor +
                    ']';
        }
    }
}
