package com.epam.indigoeln.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@JsonIgnoreProperties("id") // !!! remove after recreating the DB
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@EqualsAndHashCode(of = "username")
public class UserRef {

    @NotEmpty
    private final String username;

    @NotEmpty
    private final String displayName;
}
