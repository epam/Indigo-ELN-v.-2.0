package com.epam.indigoeln.compound.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor
public class FindSamplesRequest {

    @Nullable
    StructureSearchType structureSearchType;

    @Nullable
    String structure;
}
