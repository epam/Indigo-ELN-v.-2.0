package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.AccessLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ACLEntryPatch {

    @Nullable
    private Optional<UUID> userId;

    @Nullable
    private Optional<String> displayName;

    @Nullable
    private Optional<AccessLevel> level;

    @Nullable
    private Optional<Boolean> inherited;
}
