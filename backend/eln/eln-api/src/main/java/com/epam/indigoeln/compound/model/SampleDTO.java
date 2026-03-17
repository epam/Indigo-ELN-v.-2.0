package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SampleDTO {

    @NotNull
    private UUID id;
    @Nullable
    private NbkBatchNumber nbkBatchNumber;
    @Nullable
    private STRCodeSample strCode;
    @NotNull
    private String molecularFormula;
    @NotNull
    private BigDecimal molWeight;
    @Nullable
    private String name;
    @Nullable
    private DictionaryItemRef saltCode;
    @Nullable
    private Double saltEQ;
    @NotNull
    private UUID compoundID;
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Boolean marked;
}
