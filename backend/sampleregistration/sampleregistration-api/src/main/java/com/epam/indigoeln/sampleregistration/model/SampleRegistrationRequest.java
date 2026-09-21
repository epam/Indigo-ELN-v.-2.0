package com.epam.indigoeln.sampleregistration.model;

import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleRegistrationRequest {

    @NotNull
    private String molfile;

    @Nullable
    private UUID stereoisomerCode;

    @Nullable
    private UUID saltCode;

    @Nullable
    private Integer saltCodeNumeric;

    @Nullable
    private Integer saltEQ100;

    @NotNull
    private String nbkBatchNumber;

    @NotNull
    private Double molWeight;

    @NotNull
    private Double exactMass;

    @Nullable
    private String chemicalName;

    @Nullable
    private BigDecimal density;

    @Nullable
    private BigDecimal molarity;

    @Nullable
    private MolarityUnit molarityUnit;

    @Nullable
    private BigDecimal purity;

    @Nullable
    @Size(min = 1)
    private List<UUID> healthHazards;

    @Nullable
    private UUID compoundState;

    @Nullable
    private String batchComment;
}
