package com.epam.indigoeln.eln.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TemplateDetailsDTO extends TemplateDTO {

    private List<TemplateComponent> components;

    @Override
    public String toString() {
        return "TemplateDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
