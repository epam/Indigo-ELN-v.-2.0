package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotebookMutation extends Mutation {

    record CreateNotebook(
            @NotEmpty String name,
            @Nullable String description
    ) implements NotebookMutation {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record EditNotebookAttributes(
            @Nullable Optional<@NotEmpty String> name,
            @Nullable Optional<String> description
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

    record NotebookUndo(
            @NotNull Integer revision
    ) implements NotebookMutation {
    }

    record NotebookRedo(
            @NotNull Integer revision
    ) implements NotebookMutation {
    }
}
