package com.epam.indigoeln.compound.model.search;

import com.epam.indigoeln.common.model.search.NumericSearch;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.common.model.search.TextSearch;
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

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindSamplesRequest {

    @NotNull
    SearchCatalog catalog;

    @Nullable
    @Size(min = 1)
    String quickSearch; // My Materials, SRS: full text search; PubChem: name

    @Valid
    @Nullable
    StructuralSearch structure; // My Materials, SRS, PubChem

    @Valid
    @Nullable
    TextSearch compoundKey; // My Materials: original compoundKey; SRS: strCodeCompound

    @Valid
    @Nullable
    TextSearch casNumber; // My Materials, SRS

    @Valid
    @Nullable
    TextSearch nbkBatchNumber; // My Materials, SRS

    @Valid
    @Nullable
    TextSearch sampleKey; // My Materials: original sampleKey; SRS: strCodeSample

    @Valid
    @Nullable
    TextSearch molecularFormula; // My Materials, SRS, PubChem

    @Valid
    @Nullable
    NumericSearch molWeight; // My Materials, SRS

    @Valid
    @Nullable
    TextSearch chemicalName; // My Materials, SRS

    @Valid
    @Nullable
    ComponentStateRef compoundState; // My Materials, SRS

    @Valid
    @Nullable
    TextSearch batchComment; // My Materials, SRS

    @Valid
    @Nullable
    HealthHazardRef healthHazards; // My Materials, SRS
}
