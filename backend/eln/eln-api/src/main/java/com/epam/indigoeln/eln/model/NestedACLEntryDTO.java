package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class NestedACLEntryDTO {

    @NotNull
    EntityType entityType;

    @NotNull
    UUID entityId;

    @NotNull
    String entityName;

    @NotNull
    UUID userId;

    @NotNull
    String displayName;

    @NotNull
    AccessLevel level;
}
