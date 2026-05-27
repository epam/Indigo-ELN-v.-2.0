package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public interface ReactionOutputMutation extends Mutation {

    OutputAnchor anchor();

    record AddProductSample(
            @NotNull OutputAnchor anchor,
            @Nullable OutputSampleAnchor createdSampleAnchor
    ) implements ReactionOutputMutation {
        public AddProductSample(@NotNull OutputAnchor anchor) {
            this(anchor, null);
        }
    }

    record SetOutputRowType(
            @NotNull OutputAnchor anchor,
            @NotNull ReactionOutputType outputType
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltCode(
            @NotNull OutputAnchor anchor,
            @Nullable SaltCodeRef saltCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowSaltEQ(
            @NotNull OutputAnchor anchor,
            @Nullable Double saltEQ
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowEQ(
            @NotNull OutputAnchor anchor,
            @Nullable String eq
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowName(
            @NotNull OutputAnchor anchor,
            @NotNull String name
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowChemicalName(
            @NotNull OutputAnchor anchor,
            @Nullable String chemicalName
    ) implements ReactionOutputMutation {
    }

    record SetOutputRowIntended(
            @NotNull OutputAnchor anchor,
            @NotNull Boolean intended
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundStereoisomerCode(
            @NotNull OutputAnchor anchor,
            @Nullable StereoisomerCodeRef stereoisomerCode
    ) implements ReactionOutputMutation {
    }

    record SetOutputCompoundMolWeight(
            @NotNull OutputAnchor anchor,
            @Nullable String molWeight
    ) implements ReactionOutputMutation {
    }
}
