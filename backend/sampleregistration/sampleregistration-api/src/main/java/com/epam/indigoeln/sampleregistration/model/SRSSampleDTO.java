package com.epam.indigoeln.sampleregistration.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SRSSampleDTO {

    @Nullable
    private UUID id;
    @Nullable
    private String nbkBatchNumber;
    @NotNull
    private STRCodeCompound strCodeCompound;
    @NotNull
    private STRCodeSample strCodeSample;
    @NotNull
    private String molFormula;
    @NotNull
    private BigDecimal molWeight;
    @Nullable
    private String name;
    @Nullable
    private UUID saltCode;
    @Nullable
    private Double saltEQ;
    @Nullable
    private UUID compoundID;
    @Nullable
    private String inchi;
}
