package com.epam.indigoeln.eln.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TemplateRequest {

    @NotEmpty
    String name;

    @Valid
    @NotEmpty
    List<TemplateTab> templateTabs;
}
