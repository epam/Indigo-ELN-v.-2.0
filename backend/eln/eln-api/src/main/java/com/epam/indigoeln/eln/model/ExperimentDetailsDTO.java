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
    String title;

    @Nullable
    TherapeuticAreaRef therapeuticArea;

    @Nullable
    ProjectCodeRef projectCode;

    @Nullable
    String description;

    @Nullable
    String literature;

    @NotNull
    UUID templateId;

    @NotNull
    com.epam.indigoeln.common.model.UserRef batchCreator;

    @NotNull
    List<ExperimentRef> linkedExperiments;

    @NotNull
    List<ExperimentRef> continuedFrom;

    @NotNull
    List<ExperimentRef> continuedTo;

    @NotNull
    List<AttachmentDTO> attachments;

    @NotNull
    List<ACLDetailsEntryDTO> acl;

    @NotNull
    List<ApplicationPermission> currentPermissions;

    @NotNull
    ExperimentModel model;

    @NotNull
    UUID projectId;

    @NotNull
    String projectName;

    @NotNull
    UUID notebookId;

    @NotNull
    String notebookName;

    @Nullable
    String signatureNumber;

    @Override
    public String toString() {
        return "ExperimentDetailsDTO{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}
