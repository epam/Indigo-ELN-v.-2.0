package com.epam.indigoeln.sampleregistration.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SRSCompoundDTO {

    @NotNull
    private String canSmiles;
    @Nullable
    private UUID saltCode;
    @Nullable
    private Integer saltEQ100;
    @Nullable
    private UUID stereoisomerCode;
    @NotNull
    private String molFile;

    public SRSCompoundDTO(String canSmiles, String molFile) {
        this.canSmiles = canSmiles;
        this.molFile = molFile;
    }
}
