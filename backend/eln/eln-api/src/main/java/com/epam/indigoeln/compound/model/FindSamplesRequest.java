package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import jakarta.validation.Valid;
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
public class FindSamplesRequest {

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
    DictionaryItemRef compoundState;

    @Valid
    @Nullable
    TextSearch batchComment;

    @Valid
    @Nullable
    DictionaryItemRef healthHazards;

    @Nullable
    Boolean marked;
}
