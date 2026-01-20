package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TemplateRequest {
    @NotEmpty String name;
    @NotEmpty List<TemplateSignatureBlockRequest> signatureBlocks;
}
