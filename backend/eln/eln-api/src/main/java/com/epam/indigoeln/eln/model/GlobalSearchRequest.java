package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.compound.model.StructureSearchType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
@Builder
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
    StructureSearchType structureSearchType;

    @Nullable
    String structure;

    @JsonIgnore
    public boolean isEmpty() {
        return query == null
                && therapeuticArea == null
                && projectCode == null
                && experimentStatus == null
                && author == null
                && structureSearchType == null
                && structure == null;
    }
}
