package com.epam.indigoeln.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@EqualsAndHashCode(of = "username")
@JsonPropertyOrder({"username", "displayName"})
public class UserRef {

    @NotEmpty
    private final String username;

    @NotEmpty
    private final String displayName;
}
