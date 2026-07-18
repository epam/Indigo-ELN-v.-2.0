package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record CatalogSearchResult (
        List<SampleDTO> items,
        @Nullable Long totalItems,
        boolean hasNext
) {
}
