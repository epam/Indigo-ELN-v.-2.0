package com.epam.indigoeln.eln.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@RegisterForReflection
public class TemplateTab {

    @NotEmpty
    private String name;

    @NotEmpty
    private List<@Valid TemplateComponent> components;

    public TemplateTab(String name, List<TemplateComponent> components) {
        this.name = name;
        this.components = components;
    }
}
