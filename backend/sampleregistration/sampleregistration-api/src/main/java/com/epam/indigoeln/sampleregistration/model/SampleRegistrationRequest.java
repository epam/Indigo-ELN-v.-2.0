package com.epam.indigoeln.sampleregistration.model;

import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleRegistrationRequest {

    @NotNull
    private String molfile;

    @NotNull
    private UUID stereoisomerCode;

    @NotNull
    private UUID saltCode;

    @NotNull
    private Integer saltCodeNumeric;

    @Nullable
    private BigDecimal saltEQ;

    @NotNull
    private NbkBatchNumber nbkBatchNumber;

    @NotNull
    private Double molWeight;

    @NotNull
    private Double exactMass;

    @Nullable
    private String chemicalName;

    @Nullable
    private String casNumber;

    @Nullable
    private BigDecimal density;

    @Nullable
    private BigDecimal molarity;

    @Nullable
    private MolarityUnit molarityUnit;

    @Nullable
    private BigDecimal purity;

    @NotNull
    @Builder.Default
    private Set<UUID> healthHazards = Set.of();

    @Nullable
    private UUID compoundState;

    @Nullable
    private String batchComment;
}
