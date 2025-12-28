package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public sealed interface ExperimentMutation extends Mutation permits
        ExperimentMutation.CreateExperiment,
        ExperimentMutation.EditExperimentAttributes,
        ExperimentMutation.EditExperimentAccess,
        ExperimentMutation.CreateExperimentAttachment,
        ExperimentMutation.DeleteExperimentAttachment,
        ExperimentMutation.CancelExperiment,
        ExperimentMutation.ReopenExperiment,
        ExperimentMutation.CompleteExperiment,
        ExperimentMutation.SubmitExperiment,
        ExperimentMutation.ApproveExperiment,
        ExperimentMutation.RejectExperiment,
        ExperimentMutation.ResubmitExperiment
{

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
            List<AccessForm> edits
    ) implements ExperimentMutation {
    }

    record CreateExperimentAttachment(
            UUID attachmentID
    ) implements ExperimentMutation {
    }

    record DeleteExperimentAttachment(
            UUID attachmentID
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
            UUID signatureTemplateID
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
}
