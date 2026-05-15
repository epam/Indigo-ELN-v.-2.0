package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotebookSnapshot {

    @NotEmpty
    String name;

    @Nullable
    String description;

    @NotNull
    Set<AttachmentDTO> attachments;

    @NotNull
    Set<ACLEntryDTO> acl;
}
