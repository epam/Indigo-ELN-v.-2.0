package com.epam.indigoeln.eln.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignatureTemplateDetailsDTO extends TemplateDTO {

    private List<SignatureBlock> blocks;

    @Override
    public String toString() {
        return "SignatureTemplateDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
