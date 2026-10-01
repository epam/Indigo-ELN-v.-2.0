package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.outputsample.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface ReactionOutputSampleMutation extends ExperimentMutation {

    OutputSampleAnchor anchor();

    @Override
    default boolean isApplicableToEditSession() {
        return true;
    }

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
            @NotNull Set<HealthHazardRef> healthHazards
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
            @NotNull List<HandlingPrecautionsRef> handlingPrecautions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputStorageInstructions (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<StorageInstructionsRef> storageInstructions
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputCompoundProtection (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<CompoundProtectionRef> compoundProtection
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputSolubilityInSolvents (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<SolubidityInSolvent> solubilityInSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputResidualSolvents (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<ResidualSolvent> residualSolvents
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputMeltingPoint (
            @NotNull OutputSampleAnchor anchor,
            @Nullable MeltingPoint meltingPoint
    ) implements ReactionOutputSampleMutation {
    }

    record SetOutputPurityCalculations (
            @NotNull OutputSampleAnchor anchor,
            @NotNull List<PurityCalculation> purityCalculations
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
            @NotNull SaltCodeRef saltCode,
            @NotNull OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputSaltCode(@NotNull OutputSampleAnchor anchor, @NotNull SaltCodeRef saltCode) {
            this(anchor, saltCode, OutputAnchor.create());
        }
    }

    record SetOutputSaltEQ (
            @NotNull OutputSampleAnchor anchor,
            @Nullable Double saltEQ,
            @NotNull OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputSaltEQ(@NotNull OutputSampleAnchor anchor, @Nullable Double saltEQ) {
            this(anchor, saltEQ, OutputAnchor.create());
        }
    }

    record SetOutputStereoisomerCode (
            @NotNull OutputSampleAnchor anchor,
            @NotNull StereoisomerCodeRef stereoisomerCode,
            @NotNull OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputStereoisomerCode(@NotNull OutputSampleAnchor anchor, @NotNull StereoisomerCodeRef stereoisomerCode) {
            this(anchor, stereoisomerCode, OutputAnchor.create());
        }
    }

    record SetOutputMolfile (
            @NotNull OutputSampleAnchor anchor,
            @NotNull String molfile,
            @NotNull OutputAnchor createdOutputAnchor
    ) implements ReactionOutputSampleMutation {
        public SetOutputMolfile(@NotNull OutputSampleAnchor anchor, @NotNull String molfile) {
            this(anchor, molfile, OutputAnchor.create());
        }

        @Override
        public String toString() {
            return "SetOutputMolfile[" +
                    "anchor=" + anchor +
                    ']';
        }
    }
}
