package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SignatureTemplateRef (
        @NotNull UUID id,
        @NotNull String name
) {
}
