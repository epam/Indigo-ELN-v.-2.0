package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProjectRequest {

    @NotEmpty
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
