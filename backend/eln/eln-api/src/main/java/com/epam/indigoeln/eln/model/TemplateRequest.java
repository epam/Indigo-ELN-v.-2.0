package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemplateRequest {

    @NotEmpty
    String name;
}
