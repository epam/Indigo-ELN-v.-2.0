package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.UUID;

public interface ProjectMutation extends Mutation {

    record CreateProject(
            String name,
            @Nullable @Size(min = 1) List<String> keywords,
            @Nullable String literature,
            @Nullable String description
    ) implements ProjectMutation {
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    record EditProjectAttributes(
            JsonNullable<String> name,
            JsonNullable<List<String>> keywords,
            JsonNullable<String> literature,
            JsonNullable<String> description
    ) implements ProjectMutation {
    }

    record EditProjectAccess(
            @NotEmpty List<AccessForm> edits
    ) implements ProjectMutation {
    }

    record CreateProjectAttachment(
            @NotNull UUID attachmentID
    ) implements ProjectMutation {
    }

    record DeleteProjectAttachment(
            @NotNull UUID attachmentID
    ) implements ProjectMutation {
    }

    record ProjectAccessUpdated(
            @Nullable String notebookName,
            @Nullable String experimentName
    ) implements ProjectMutation {
    }

    record ProjectUndo(
    ) implements ProjectMutation {
    }

    record ProjectRedo(
    ) implements ProjectMutation {
    }
}
