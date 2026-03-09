package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class NotebookExistenceCheckDTO {
    @NotNull
    Boolean exists;
}