package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ExperimentRequest {

    @NotNull
    UUID templateID;

    @Nullable
    String description;

    @Nullable
    DictionaryRef therapeuticArea;

    @Nullable
    DictionaryRef projectCode;

    public ExperimentRequest(UUID templateID) {
        this(templateID, null, null, null);
    }
}
