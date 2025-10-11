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
    TextSearch notebookBatchNumber;

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
}
