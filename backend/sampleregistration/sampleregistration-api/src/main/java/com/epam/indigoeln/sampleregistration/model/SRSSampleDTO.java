package com.epam.indigoeln.sampleregistration.model;

import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SRSSampleDTO {

    @NotNull
    private UUID id;
    @NotNull
    private UUID compoundID;
    @Nullable
    private NbkBatchNumber nbkBatchNumber;
    @NotNull
    private STRCodeCompound strCodeCompound;
    @NotNull
    private STRCodeSample strCodeSample;
    @NotNull
    private String molFormula;
    @NotNull
    private BigDecimal molWeight;
    @Nullable
    private String chemicalName;
    @Nullable
    private String name;
    @NotNull
    private UUID saltCode;
    @Nullable
    private Double saltEQ;
    @Nullable
    private BigDecimal density;
    @Nullable
    private BigDecimal molarity;
    @Nullable
    private MolarityUnit molarityUnit;
    @Nullable
    private BigDecimal purity;
    @NotNull
    private Set<UUID> healthHazards = Set.of();
    @Nullable
    private UUID compoundState;
    @Nullable
    private String batchComment;

    public SRSSampleDTO(UUID id, UUID compoundID, UUID saltCode, STRCodeCompound strCodeCompound, STRCodeSample strCodeSample, String molFormula, BigDecimal molWeight) {
        this.id = id;
        this.compoundID = compoundID;
        this.saltCode = saltCode;
        this.strCodeCompound = strCodeCompound;
        this.strCodeSample = strCodeSample;
        this.molFormula = molFormula;
        this.molWeight = molWeight;
    }
}
