package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ExperimentRequest {

    @NotEmpty
    String name;

    @Nullable
    String description;

    @Nullable
    DictionaryRef therapeuticArea;

    @Nullable
    DictionaryRef projectCode;

    public ExperimentRequest(@NotEmpty String name) {
        this(name, null, null, null);
    }
}
