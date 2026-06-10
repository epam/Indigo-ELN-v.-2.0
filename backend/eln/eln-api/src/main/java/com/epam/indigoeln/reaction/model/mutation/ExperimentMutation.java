package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ProjectCodeRef;
import com.epam.indigoeln.eln.model.TherapeuticAreaRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ExperimentMutation extends Mutation {

    record CreateExperiment(
            UUID templateID,
            @Nullable String description,
            @Nullable TherapeuticAreaRef therapeuticArea,
            @Nullable ProjectCodeRef projectCode
    ) implements ExperimentMutation {
    }

//    record DeleteExperiment(
//    ) implements ExperimentMutation {
//    }

    record SetExperimentSignificantFigures(
            @NotNull @Min(1) @Max(5) Integer significantFigures
    ) implements ExperimentMutation {

        @Override
        public boolean isApplicableToEditSession() {
            return true;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    record EditExperimentAttributes(
            JsonNullable<String> title,
            JsonNullable<TherapeuticAreaRef> therapeuticArea,
            JsonNullable<ProjectCodeRef> projectCode,
            JsonNullable<String> description,
            JsonNullable<String> literature,
            JsonNullable<Set<ExperimentRef>> linkedExperiments,
            JsonNullable<Set<ExperimentRef>> continuedFrom,
            JsonNullable<Set<ExperimentRef>> continuedTo
    ) implements ExperimentMutation {
    }

    record EditExperimentAccess(
            @NotEmpty List<AccessForm> edits
    ) implements ExperimentMutation {
    }

    record SetBatchCreator(
            @NotNull UserRef batchCreator
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

    record SignatureUpdated(
            @NotNull String message,
            @NotNull DocumentStatus documentStatus,
            @NotNull UUID attachmentID
    ) implements ExperimentMutation {
    }

    record MakeVersion(
    ) implements ExperimentMutation {
    }

    record ExperimentAccessUpdated(
            @Nullable String projectName,
            @Nullable String notebookName
    ) implements ExperimentMutation {
    }

    record Undo(
    ) implements ExperimentMutation {
    }

    record Redo(
    ) implements ExperimentMutation {
    }
}
