package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.UserRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentPatch {

    @Nullable
    private Patched<UserRef, UserRef> createdBy;

    @Nullable
    private Patched<ZonedDateTime, ZonedDateTime> createdAt;

    @Nullable
    private Patched<UserRef, UserRef> modifiedBy;

    @Nullable
    private Patched<ZonedDateTime, ZonedDateTime> modifiedAt;

    @Nullable
    private Patched<String, String> name;

    @Nullable
    private Patched<Long, Long> size;
}
