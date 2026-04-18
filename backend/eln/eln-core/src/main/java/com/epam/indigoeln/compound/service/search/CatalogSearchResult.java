package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record CatalogSearchResult (
        List<SampleDTO> items,
        @Nullable String nextAfter,
        @Nullable Long totalItems
) {
    int size() {
        return items.size();
    }

    boolean hasNext() {
        return nextAfter != null;
    }
}
