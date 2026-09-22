package com.epam.indigoeln.signature.model;

import com.epam.indigoeln.common.model.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "name"})
public class SignatureTemplateDTO extends BaseDTO {

    @NotEmpty
    String name;

    public SignatureTemplateDTO(UUID id, String name) {
        setId(id);
        this.name = name;
    }
}
