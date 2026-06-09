package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ExperimentSnapshot implements ExperimentNode {

    private int revision;

    @Nullable
    private String title;

    private ExperimentStatus status;

    @Nullable
    private TherapeuticAreaRef therapeuticArea;

    @Nullable
    private ProjectCodeRef projectCode;

    @Nullable
    private String description;

    @Nullable
    String literature;

    @NotNull
    UserRef batchCreator;

    @NotNull
    UUID templateId;

    @NotNull
    Set<ExperimentRef> linkedExperiments;

    @NotNull
    Set<ExperimentRef> continuedFrom;

    @NotNull
    Set<ExperimentRef> continuedTo;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean deleted;

    @NotNull
    private Set<AttachmentDTO> attachments;

    @NotNull
    private Set<ACLEntryDTO> acl;

    @NotNull
    private ExperimentModel model;
}
