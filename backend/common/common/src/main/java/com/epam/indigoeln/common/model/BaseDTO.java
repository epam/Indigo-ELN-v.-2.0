package com.epam.indigoeln.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public abstract class BaseDTO {

    @NotNull
    UUID id;

    @NotNull
    UserRef createdBy;

    @NotNull
    Instant createdAt;

    @NotNull
    UserRef modifiedBy;

    @NotNull
    Instant modifiedAt;
}
