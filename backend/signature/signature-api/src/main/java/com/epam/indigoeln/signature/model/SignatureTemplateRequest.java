package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SignatureTemplateRequest {

    @NotEmpty
    String name;

    @NotNull
    List<SignatureTemplateBlock> blocks;
}
