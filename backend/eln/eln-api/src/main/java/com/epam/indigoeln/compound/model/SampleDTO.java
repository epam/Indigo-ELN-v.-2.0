package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.compound.model.search.SearchCatalog;
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
    SearchCatalog source;
    @Nullable
    private UUID id;
    @Nullable
    private NbkBatchNumber nbkBatchNumber;
    @Nullable
    private String compoundKey;
    @Nullable
    private STRCodeSample strCode;
    @NotNull
    private String molFormula;
    @NotNull
    private BigDecimal molWeight;
    @Nullable
    private String name;
    @Nullable
    private DictionaryItemRef saltCode;
    @Nullable
    private Double saltEQ;
    @Nullable
    private UUID compoundID;
    @Nullable
    private String inchi;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean marked;
}
