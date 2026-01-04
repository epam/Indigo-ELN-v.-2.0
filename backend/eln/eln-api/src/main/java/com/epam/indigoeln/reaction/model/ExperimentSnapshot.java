package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ExperimentSnapshot implements ExperimentNode {

    private Integer revision;

    private ExperimentStatus status;

    @Nullable
    private DictionaryItemRef therapeuticArea;

    @Nullable
    private DictionaryItemRef projectCode;

    @Nullable
    private String description;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Boolean deleted;

    @Nullable
    private Set<AttachmentDTO> attachments;

    @Nullable
    private Set<ACLDetailsEntryDTO> acl;

    @Nullable
    private ExperimentModel model;
}
