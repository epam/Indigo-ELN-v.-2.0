package com.epam.indigoeln.eln.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateDetailsDTO extends TemplateDTO {

    @Override
    public String toString() {
        return "TemplateDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
