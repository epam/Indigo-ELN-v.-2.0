package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.SampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

abstract class AbstractELNCatalogSearchProvider implements CatalogSearchProvider {

    @Inject
    SampleRepository sampleRepository;

    @Override
    public CatalogSearchResult search(FindSamplesRequest request, int pageNo, int pageSize) {
        Page<SampleDTO> page = doSearch(request, pageNo, pageSize);
        List<SampleDTO> list = page.getItems();
        long nextResult = (long) (page.getPageNo() + 1) * page.getPageSize();
        boolean hasNext = page.getTotalItems() > nextResult;
        return new CatalogSearchResult(list, page.getTotalItems(), hasNext);
    }

    @Override
    public SampleEntity importSample(SampleDTO searchItem) {
        throw new UnsupportedOperationException("Sample already exists in ELN");
    }

    protected abstract Page<SampleDTO> doSearch(FindSamplesRequest request, int pageNo, int pageSize);
}

@ApplicationScoped
class ELNCatalogSearchProvider extends AbstractELNCatalogSearchProvider {

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.ELN;
    }

    @Override
    protected Page<SampleDTO> doSearch(FindSamplesRequest request, int pageNo, int pageSize) {
        return sampleRepository.find(request, null, pageNo, pageSize);
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

    @Override
    protected Page<SampleDTO> doSearch(FindSamplesRequest request, int pageNo, int pageSize) {
        return sampleRepository.find(request, true, pageNo, pageSize);
    }
}
