package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.Nullable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class FindSamplesRequest {

    @Nullable
    @Length(min = 1)
    String quickSearch;

    @Valid
    @Nullable
    StructuralSearch structure;

    @Valid
    @Nullable
    TextSearch strCode;

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
