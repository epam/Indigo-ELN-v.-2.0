package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProjectRequest {

    String name;

    @NotNull
    Set<String> keywords;

    @Nullable
    String literature;

    @Nullable
    String description;

    public ProjectRequest(String name) {
        this(name, Set.of(), null, null);
    }
}
