package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentPatch {

    // !!! remove revision when writing to audit log tables, because revision is already a table field
    private Patched<Integer, Integer> revision;

    @Nullable
    private Patched<ExperimentStatus, ExperimentStatus> status;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> therapeuticArea;

    @Nullable
    private Patched<DictionaryItemRef, DictionaryItemRef> projectCode;

    @Nullable
    private Patched<String, String> description;

    @Nullable
    private Patched<Boolean, Boolean> deleted;

    @Nullable
    private Patched<Set<AttachmentDTO>, Map<UUID, Patched<AttachmentDTO, AttachmentPatch>>> attachments;

    @Nullable
    private Patched<Set<ACLDetailsEntryDTO>, Map<String, Patched<ACLDetailsEntryDTO, ACLEntryPatch>>> acl;

    @Nullable
    private Patched<ExperimentModel, ExperimentModelPatch> model;
}
