package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentDetailsDTO extends BaseExperimentDTO {

    @Nullable
    DictionaryItemRef therapeuticArea;

    @Nullable
    DictionaryItemRef projectCode;

    @Nullable
    String description;

    @NotNull
    UUID templateId;

    @NotNull
    List<AttachmentDTO> attachments;

    @NotNull
    List<ACLDetailsEntryDTO> acl;

    @NotNull
    List<ExperimentSignature> signatures;

    @NotNull
    List<ApplicationPermission> currentPermissions;

    @NotNull
    Integer revision;

    @NotNull
    ExperimentModel model;

    @Override
    public String toString() {
        return "ExperimentDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
