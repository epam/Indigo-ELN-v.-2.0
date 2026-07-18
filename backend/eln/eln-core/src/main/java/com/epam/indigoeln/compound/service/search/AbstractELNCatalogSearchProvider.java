package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.SampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractELNCatalogSearchProvider implements CatalogSearchProvider {

    @Inject
    SampleRepository sampleRepository;

    @Override
    public CatalogSearchResult search(FindSamplesRequest request, @Nullable String nextAfter, int limit) {
        Pair<List<SampleDTO>, Long> pair = doSearch(request, nextAfter, limit + 1); // request 1 extra item to determine if there is a next page
        List<SampleDTO> list = checkNotNull(pair.a());
        if (list.size() > limit) { // more results available
            list = list.subList(0, list.size() - 1);
            return new CatalogSearchResult(list, checkNotNull(list.getLast().getId()).toString(), pair.b());
        }
        // empty or non-empty list, but there will be no more results
        return new CatalogSearchResult(list, null, pair.b());
    }

    @Override
    public SampleEntity importSample(SampleDTO searchItem) {
        throw new UnsupportedOperationException("Sample already exists in ELN");
    }

    protected abstract Pair<List<SampleDTO>, Long> doSearch(FindSamplesRequest request, @Nullable String nextAfter, int limit);
}

@ApplicationScoped
class ELNCatalogSearchProvider extends AbstractELNCatalogSearchProvider {

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.ELN;
    }

    protected Pair<List<SampleDTO>, Long> doSearch(FindSamplesRequest request, @Nullable String nextAfter, int limit) {
        return sampleRepository.find(request, null, limit, nextAfter != null ? UUID.fromString(nextAfter) : null);
    }
}

@ApplicationScoped
class MyMaterialsCatalogSearchProvider extends AbstractELNCatalogSearchProvider {

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.MY_MATERIALS;
    }

    @Override
    public boolean isEnabled(FindSamplesRequest request) {
        return !request.getCatalogs().contains(SearchCatalog.ELN); // if ELN is searched, it will already return both marked and non-marked samples
    }

    protected Pair<List<SampleDTO>, Long> doSearch(FindSamplesRequest request, @Nullable String nextAfter, int limit) {
        return sampleRepository.find(request, true, limit, nextAfter != null ? UUID.fromString(nextAfter) : null);
    }
}
