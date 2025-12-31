package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ACLEntryPatch {

    @Nullable
    private Patched<UUID> userId;

    @Nullable
    private Patched<String> displayName;

    @Nullable
    private Patched<AccessLevel> level;

    @Nullable
    private Patched<Boolean> inherited;
}
