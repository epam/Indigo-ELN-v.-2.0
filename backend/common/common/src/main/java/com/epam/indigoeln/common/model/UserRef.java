package com.epam.indigoeln.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@EqualsAndHashCode(of = "username")
public class UserRef {

    @NotEmpty
    private final String username;

    @NotEmpty
    private final String displayName;
}
