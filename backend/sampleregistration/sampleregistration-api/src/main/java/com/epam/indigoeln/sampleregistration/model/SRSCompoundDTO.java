package com.epam.indigoeln.sampleregistration.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SRSCompoundDTO {

    @NotNull
    private String canSmiles;
    @NotNull
    private UUID saltCode;
    @Nullable
    private BigDecimal saltEQ;
    @NotNull
    private UUID stereoisomerCode;
    @NotNull
    private String molFile;

    public SRSCompoundDTO(String canSmiles, UUID stereoisomerCode, UUID saltCode, String molFile) {
        this.canSmiles = canSmiles;
        this.stereoisomerCode = stereoisomerCode;
        this.saltCode = saltCode;
        this.molFile = molFile;
    }
}
