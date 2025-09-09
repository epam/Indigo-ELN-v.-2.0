package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TemplateRequest {

    @NotEmpty
    String name;

    @NotEmpty
    List<TemplateTab> templateTabs;
}
