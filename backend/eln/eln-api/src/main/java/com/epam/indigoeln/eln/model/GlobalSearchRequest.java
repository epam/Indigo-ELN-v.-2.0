package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.compound.model.StructuralSearch;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.jspecify.annotations.Nullable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class GlobalSearchRequest {

    @Nullable
    String query;

    @Nullable
    DictionaryItemRef therapeuticArea;

    @Nullable
    DictionaryItemRef projectCode;

    @Nullable
    ExperimentStatus experimentStatus;

    @Nullable
    UserRef author;

    // Batch Yield %

    // Batch Purity %

    @Nullable
    StructuralSearch structure;

    @JsonIgnore
    public boolean isEmpty() {
        return query == null
                && therapeuticArea == null
                && projectCode == null
                && experimentStatus == null
                && author == null
                && structure == null;
    }
}
