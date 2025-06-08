package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExperimentDetailsDTO extends ExperimentDTO {

    DictionaryRef therapeuticArea;

    DictionaryRef projectCode;

    @NotNull
    List<AttachmentDTO> attachments;

    @Override
    public String toString() {
        return "ExperimentDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
