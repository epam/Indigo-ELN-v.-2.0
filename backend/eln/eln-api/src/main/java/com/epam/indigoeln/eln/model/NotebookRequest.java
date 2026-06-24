package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class NotebookRequest {

    String name;

    @Nullable
    String description;

    public NotebookRequest(String name) {
        this(name, null);
    }
}
