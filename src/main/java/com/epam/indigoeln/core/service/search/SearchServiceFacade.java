package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Service
public class SearchServiceFacade {

    @Autowired
    private List<SearchServiceAPI> catalogues;

    public Collection<SearchServiceAPI.Info> getCatalogues() {
        return catalogues.stream().map(SearchServiceAPI::getInfo).collect(Collectors.toList());
    }

    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        Collection<ProductBatchDetailsDTO> result = new ArrayList<>();
        for(SearchServiceAPI provider : getSearchProviders(searchRequest.getDatabases())) {
            result.addAll(provider.findBatches(searchRequest));
        }
        return result;
    }

    private Collection<SearchServiceAPI> getSearchProviders(List<String> dataSourceNames) {
        return catalogues.stream().filter(p -> dataSourceNames.contains(p.getInfo().getValue())).collect(Collectors.toList());
    }

}
