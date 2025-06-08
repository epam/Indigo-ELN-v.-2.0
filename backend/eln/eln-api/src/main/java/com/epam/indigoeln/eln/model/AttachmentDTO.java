package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    Long size;

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
