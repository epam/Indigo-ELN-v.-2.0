package com.epam.indigoeln.eln.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.List;

@RegisterForReflection
@Data
public class TemplateTab {

    private String name;

    private List<TemplateComponent> components;

    public TemplateTab(String name, List<TemplateComponent> components) {
        this.name = name;
        this.components = components;
    }
}
