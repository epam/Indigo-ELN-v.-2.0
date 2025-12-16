package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class BaseNotebookDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    Map<ExperimentStatus, Integer> experimentCount;
}
