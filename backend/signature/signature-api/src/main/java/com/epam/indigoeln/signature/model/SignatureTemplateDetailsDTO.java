package com.epam.indigoeln.signature.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureTemplateDetailsDTO extends SignatureTemplateDTO {

    private List<SignatureTemplateBlock> blocks;
}
