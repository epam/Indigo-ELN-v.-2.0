package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateDTO extends BaseDTO {

    @NotEmpty
    String name;

    @Override
    public String toString() {
        return "TemplateDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
