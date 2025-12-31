package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.*;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
public class ExperimentSnapshot {

    private Integer revision;

    private ExperimentStatus status;

    @Nullable
    private DictionaryItemRef therapeuticArea;

    @Nullable
    private DictionaryItemRef projectCode;

    @Nullable
    private String description;

    private Boolean deleted;

    @Nullable
    private Set<AttachmentDTO> attachments;

    @Nullable
    private Set<ACLDetailsEntryDTO> acl;

    @Nullable
    private ExperimentModel model;
}
