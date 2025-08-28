package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExperimentForSignatureDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    List<ExperimentSignature> signatures;
}
