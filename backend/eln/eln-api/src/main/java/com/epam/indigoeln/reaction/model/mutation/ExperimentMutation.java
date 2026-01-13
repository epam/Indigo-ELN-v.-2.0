package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperimentMutation extends Mutation {

    record CreateExperiment(
            UUID templateID,
            @Nullable String description,
            @Nullable DictionaryItemRef therapeuticArea,
            @Nullable DictionaryItemRef projectCode
    ) implements ExperimentMutation {
    }

//    record DeleteExperiment(
//    ) implements ExperimentMutation {
//    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record EditExperimentAttributes(
            @Nullable Optional<DictionaryItemRef> therapeuticArea,
            @Nullable Optional<DictionaryItemRef> projectCode
    ) implements ExperimentMutation {
    }

    record EditExperimentAccess(
            @NotEmpty List<AccessForm> edits
    ) implements ExperimentMutation {
    }

    record CreateExperimentAttachment(
            @NotNull UUID attachmentID
    ) implements ExperimentMutation {
    }

    record DeleteExperimentAttachment(
            @NotNull UUID attachmentID
    ) implements ExperimentMutation {
    }

    record CancelExperiment(
    ) implements ExperimentMutation {
    }

    record ReopenExperiment(
    ) implements ExperimentMutation {
    }

    record CompleteExperiment(
    ) implements ExperimentMutation {
    }

    record SubmitExperiment(
            @NotNull UUID signatureTemplateID
    ) implements ExperimentMutation {
    }

    record ApproveExperiment(
    ) implements ExperimentMutation {
    }

    record RejectExperiment(
    ) implements ExperimentMutation {
    }

    record ResubmitExperiment(
    ) implements ExperimentMutation {
    }

    record Undo(
            @NotNull Integer revision
    ) implements ExperimentMutation {
    }

    record Redo(
            @NotNull Integer revision
    ) implements ExperimentMutation {
    }
}
