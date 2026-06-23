package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ProjectRequest {

    String name;

    @Nullable
    List<String> keywords;

    @Nullable
    String literature;

    @Nullable
    String description;

    public ProjectRequest(String name) {
        this(name, null, null, null);
    }
}
