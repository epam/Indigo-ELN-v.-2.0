package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseExperimentDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    ExperimentStatus status;

    @NotNull
    Boolean marked;
}
