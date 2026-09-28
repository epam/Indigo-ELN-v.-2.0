package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;

public interface CatalogSearchProvider {

    SearchCatalog catalog();

    default boolean isEnabled(FindSamplesRequest request) {
        return true;
    }

    CatalogSearchResult search(FindSamplesRequest request, int pageNo, int pageSize);

    SampleEntity importSample(SampleDTO searchItem);
}
