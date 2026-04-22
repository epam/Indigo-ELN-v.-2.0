package com.epam.indigoeln.compound.model.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record SampleSearchResult (
        List<SampleDTO> items,
        boolean hasNext,
        @Nullable SearchCatalog nextCatalog,
        @Nullable String nextAfter,
        @Nullable Long totalItems
) {}
