package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureTemplateDTO extends BaseDTO {

    @NotEmpty
    String name;

    @Override
    public String toString() {
        return "SignatureTemplateDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
