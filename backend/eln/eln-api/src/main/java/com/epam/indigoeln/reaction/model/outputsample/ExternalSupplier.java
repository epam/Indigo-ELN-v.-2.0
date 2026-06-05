package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.ExternalSupplierRef;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ExternalSupplier {

    @NotNull
    private final ExternalSupplierRef supplier;

    @NotNull
    private final String registryNumber;
}
