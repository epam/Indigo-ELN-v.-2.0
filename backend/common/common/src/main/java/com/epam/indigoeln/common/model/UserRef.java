package com.epam.indigoeln.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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
