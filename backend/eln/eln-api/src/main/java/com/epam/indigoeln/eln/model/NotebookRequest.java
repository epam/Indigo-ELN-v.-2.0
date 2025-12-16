package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class NotebookRequest {

    @NotEmpty
    String name;

    @Nullable
    String description;

    public NotebookRequest(@NotEmpty String name) {
        this(name, null);
    }
}
