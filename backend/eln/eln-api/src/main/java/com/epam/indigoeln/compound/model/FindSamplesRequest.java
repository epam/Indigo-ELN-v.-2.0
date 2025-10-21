package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
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
    String quickSearch;

    @Nullable
    StructuralSearch structure;

    @Nullable
    TextSearch strCode;

    @Nullable
    TextSearch nbkBatchNumber;

    @Nullable
    TextSearch casNumber;

    @Nullable
    TextSearch externalNumber;

    @Nullable
    TextSearch molecularFormula;

    @Nullable
    NumericSearch molWeight;

    @Nullable
    TextSearch chemicalName;

    @Nullable
    DictionaryItemRef compoundState;

    @Nullable
    TextSearch batchComment;

    @Nullable
    DictionaryItemRef healthHazards;
}
