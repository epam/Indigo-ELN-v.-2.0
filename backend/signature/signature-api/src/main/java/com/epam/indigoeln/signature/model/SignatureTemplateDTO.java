package com.epam.indigoeln.signature.model;

import com.epam.indigoeln.common.model.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = {"id", "name"})
public class SignatureTemplateDTO extends BaseDTO {

    @NotEmpty
    String name;
}
