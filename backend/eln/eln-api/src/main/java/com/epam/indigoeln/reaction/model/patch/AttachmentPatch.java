package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.UserRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class AttachmentPatch {

    @Nullable
    private Optional<UUID> id;

    @Nullable
    private Optional<UserRef> createdBy;

    @Nullable
    private Optional<ZonedDateTime> createdAt;

    @Nullable
    private Optional<UserRef> modifiedBy;

    @Nullable
    private Optional<ZonedDateTime> modifiedAt;

    @Nullable
    private Optional<String> name;

    @Nullable
    private Optional<Long> size;
}
