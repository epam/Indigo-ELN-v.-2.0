package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseDTO {

    @NotNull
    UUID id;

    @NotNull
    UserRef createdBy;

    @NotNull
    ZonedDateTime createdAt;

    @NotNull
    UserRef modifiedBy;

    @NotNull
    ZonedDateTime modifiedAt;
}
