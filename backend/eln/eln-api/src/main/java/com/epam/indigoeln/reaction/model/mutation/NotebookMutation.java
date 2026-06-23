package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.UUID;

public interface NotebookMutation extends Mutation {

    record CreateNotebook(
            String name,
            @Nullable String description
    ) implements NotebookMutation {
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    record EditNotebookAttributes(
            JsonNullable<String> name,
            JsonNullable<String> description
    ) implements NotebookMutation {
    }

    record EditNotebookAccess(
            @NotEmpty List<AccessForm> edits
    ) implements NotebookMutation {
    }

    record CreateNotebookAttachment(
            @NotNull UUID attachmentID
    ) implements NotebookMutation {
    }

    record DeleteNotebookAttachment(
            @NotNull UUID attachmentID
    ) implements NotebookMutation {
    }

    record NotebookAccessUpdated(
            @Nullable String projectName,
            @Nullable String experimentName
    ) implements NotebookMutation {
    }

    record NotebookUndo(
    ) implements NotebookMutation {
    }

    record NotebookRedo(
    ) implements NotebookMutation {
    }
}
