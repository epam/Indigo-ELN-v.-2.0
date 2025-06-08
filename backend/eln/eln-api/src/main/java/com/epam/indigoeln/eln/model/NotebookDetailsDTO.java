package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class NotebookDetailsDTO extends NotebookDTO {

    @Nullable
    String description;

    @NotNull
    List<AttachmentDTO> attachments;

    @Override
    public String toString() {
        return "NotebookDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
