package com.epam.indigoeln.compound.model.search;

import com.epam.indigoeln.eln.model.ComponentStateRef;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindSamplesRequest {

    @NotNull
    @Size(min = 1)
    Set<SearchCatalog> catalogs;

    @Nullable
    @Size(min = 1)
    String quickSearch;

    @Valid
    @Nullable
    StructuralSearch structure;

    @Valid
    @Nullable
    TextSearch compoundKey;

    @Valid
    @Nullable
    TextSearch nbkBatchNumber;

    @Valid
    @Nullable
    TextSearch casNumber;

    @Valid
    @Nullable
    TextSearch externalNumber;

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
    ComponentStateRef compoundState;

    @Valid
    @Nullable
    TextSearch batchComment;

    @Valid
    @Nullable
    HealthHazardRef healthHazards;
}
