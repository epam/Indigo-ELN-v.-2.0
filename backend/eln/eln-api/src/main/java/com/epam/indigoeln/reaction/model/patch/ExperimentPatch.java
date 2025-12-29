package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExperimentPatch {

    @Nullable
    private Optional<ExperimentStatus> status;

    @Nullable
    private Optional<DictionaryItemRef> therapeuticArea;

    @Nullable
    private Optional<DictionaryItemRef> projectCode;

    @Nullable
    private Optional<String> description;

    @Nullable
    private Optional<Boolean> deleted;

    @Nullable
    private Optional<ListPatch<UUID, AttachmentPatch>> attachments;

    @Nullable
    private Optional<ListPatch<UUID, ACLEntryPatch>> acl;

    @Nullable
    private Optional<ExperimentModelPatch> model;
}
