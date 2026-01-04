package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentPatch {

    // !!! remove revision when writing to audit log tables, because revision is already a table field
    private Patched<Integer> revision;

    @Nullable
    private Patched<ExperimentStatus> status;

    @Nullable
    private Patched<DictionaryItemRef> therapeuticArea;

    @Nullable
    private Patched<DictionaryItemRef> projectCode;

    @Nullable
    private Patched<String> description;

    @Nullable
    private Patched<Boolean> deleted;

    @Nullable
    private Patched<Map<UUID, Patched<AttachmentPatch>>> attachments;

    @Nullable
    private Patched<Map<String, Patched<ACLEntryPatch>>> acl;

    @Nullable
    private Patched<ExperimentModelPatch> model;
}
