package com.epam.indigoeln.signature.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemplateSignatureBlockRequest {
    String username;
    @NotNull Reason reason;
}
