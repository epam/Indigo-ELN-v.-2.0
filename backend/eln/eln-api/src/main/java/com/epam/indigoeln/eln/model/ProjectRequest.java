package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProjectRequest {

    @NotEmpty(message = "Project Name is required")
    @Size(max = 256, message = "Project name must be at most 256 characters")
    String name;

    @Nullable
    List<@NotEmpty String> keywords;

    @Nullable
    String literature;

    @Nullable
    String description;

    public ProjectRequest(@NotEmpty String name) {
        this(name, null, null, null);
    }
}
