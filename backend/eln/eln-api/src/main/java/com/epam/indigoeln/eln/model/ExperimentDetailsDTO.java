package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class ExperimentDetailsDTO extends BaseExperimentDTO {

    @Nullable
    DictionaryItemRef therapeuticArea;

    @Nullable
    DictionaryItemRef projectCode;

    @Nullable
    String description;

    @NotNull
    List<AttachmentDTO> attachments;

    @NotNull
    List<ACLDetailsEntryDTO> acl;

    @NotNull
    List<ExperimentSignature> signatures;

    @Override
    public String toString() {
        return "ExperimentDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
