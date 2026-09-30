package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.model.ComponentStateRef;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.SampleSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class SampleDTO {

    @NotNull
    SearchCatalog catalog;

    // compound key
    @NotNull
    SampleSource source;
    @NotNull
    private String compoundKey;
    @Nullable
    private UUID compoundID; // when ID in source system is known (ELN and SRS)
    @Nullable
    private SaltCodeRef saltCode;
    @Nullable
    private Double saltEQ;
    @Nullable
    private String chemicalName;
    @Nullable
    private String inchi; // PubChem only: the sole way to render its structure

    // sample characteristics
    @NotNull
    private String sampleKey;
    @Nullable
    private NbkBatchNumber nbkBatchNumber;
    @NotNull
    private String molFormula;
    @NotNull
    private BigDecimal molWeight;
    @Nullable
    private BigDecimal density;
    @Nullable
    private BigDecimal molarity;
    @Nullable
    private MolarityUnit molarityUnit;
    @Nullable
    private BigDecimal purity;
    @Nullable
    private List<HealthHazardRef> healthHazards;
    @Nullable
    private ComponentStateRef compoundState;
    @Nullable
    private String batchComment;

    // other
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean marked;
}
