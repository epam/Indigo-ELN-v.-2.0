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
    private Patched<UserRef> createdBy;

    @Nullable
    private Patched<ZonedDateTime> createdAt;

    @Nullable
    private Patched<UserRef> modifiedBy;

    @Nullable
    private Patched<ZonedDateTime> modifiedAt;

    @Nullable
    private Patched<String> name;

    @Nullable
    private Patched<Long> size;
}
