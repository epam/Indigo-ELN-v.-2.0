package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NotebookDetailsDTO extends BaseNotebookDTO {

    @NotNull
    Integer revision;

    @Nullable
    String description;

    @NotNull
    List<AttachmentDTO> attachments;

    @NotNull
    List<ACLEntryDTO> acl;

    @NotNull
    List<ApplicationPermission> currentPermissions;

    @NotNull
    UUID projectId;

    @NotNull
    String projectName;

    @Override
    public String toString() {
        return "NotebookDetailsDTO{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}
