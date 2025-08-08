package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class UserRef {

    @NotNull
    UUID id;

    @NotEmpty
    String username;

    @NotEmpty
    String displayName;
}
