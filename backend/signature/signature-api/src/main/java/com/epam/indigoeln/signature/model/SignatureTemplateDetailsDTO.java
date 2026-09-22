package com.epam.indigoeln.signature.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignatureTemplateDetailsDTO extends SignatureTemplateDTO {

    private List<SignatureTemplateBlock> blocks;
}
