package com.epam.indigoeln.compound.model.search;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record FindSamplesState (
        List<SearchCatalog> catalogs,
        int pageNo,
        int pageSize,
        @Nullable Long oldCatalogsTotalItems
) {}
