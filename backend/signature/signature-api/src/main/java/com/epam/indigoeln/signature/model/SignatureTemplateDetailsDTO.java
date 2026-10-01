package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignatureTemplateDetailsDTO extends SignatureTemplateDTO {

    @NotNull
    private List<SignatureTemplateBlock> blocks;
}
