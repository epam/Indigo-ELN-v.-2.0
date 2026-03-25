package com.epam.indigoeln.reaction.model.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMutation extends Mutation {

    record CreateProject(
            @NotEmpty String name,
            @Nullable @Size(min = 1) List<@NotEmpty String> keywords,
            @Nullable String literature,
            @Nullable String description
    ) implements ProjectMutation {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record EditProjectAttributes(
            @Nullable Optional<@NotEmpty String> name,
            @Nullable Optional<List<@NotEmpty String>> keywords,
            @Nullable Optional<String> literature,
            @Nullable Optional<String> description
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
