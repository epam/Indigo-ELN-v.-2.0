package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotebookPatch {

    @Nullable
    private Patched<String, String> name;

    @Nullable
    private Patched<String, String> description;

    @Nullable
    private Patched<Set<AttachmentDTO>, Map<UUID, Patched<AttachmentDTO, AttachmentPatch>>> attachments;

    @Nullable
    private Patched<Set<ACLDetailsEntryDTO>, Map<String, Patched<ACLDetailsEntryDTO, ACLEntryPatch>>> acl;
}
