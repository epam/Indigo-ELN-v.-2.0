package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

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

    // chemical search

    @JsonIgnore
    public boolean isEmpty() {
        return query == null
               && therapeuticArea == null
               && projectCode == null
               && experimentStatus == null
               && author == null;
    }
}
