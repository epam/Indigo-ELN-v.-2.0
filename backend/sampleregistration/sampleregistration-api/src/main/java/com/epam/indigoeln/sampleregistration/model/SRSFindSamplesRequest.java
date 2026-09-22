package com.epam.indigoeln.sampleregistration.model;

import com.epam.indigoeln.common.model.search.NumericSearch;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.common.model.search.TextSearch;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SRSFindSamplesRequest {

    @Nullable
    @Size(min = 1)
    String quickSearch;

    @Valid
    @Nullable
    StructuralSearch structure;

    @Valid
    @Nullable
    TextSearch strCodeSample;

    @Valid
    @Nullable
    TextSearch molecularFormula;

    @Valid
    @Nullable
    NumericSearch molWeight;

    @Valid
    @Nullable
    TextSearch chemicalName;

    @Valid
    @Nullable
    UUID compoundState;

    @Valid
    @Nullable
    TextSearch batchComment;

    @Valid
    @Nullable
    UUID healthHazards;
}
