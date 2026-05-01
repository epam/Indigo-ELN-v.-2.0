package com.epam.indigoeln.reaction.model.outputsample;

import com.epam.indigoeln.eln.model.ExternalSupplierRef;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSupplier {

    @NotNull
    private ExternalSupplierRef supplier;

    @NotNull
    private String registryNumber;
}
