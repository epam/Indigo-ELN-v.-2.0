package com.epam.indigoeln.compound.model;

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
    StructureSearchType structureSearchType;

    @Nullable
    String structure;
}
