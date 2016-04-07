package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Service("commonSearchService")
public class SearchServiceFacade implements SearchServiceAPI {

    @Autowired
    @Qualifier("customSearchService")
    private SearchServiceAPI customSearchService;


    @Override
    public Collection<ProductBatchDetailsDTO> searchBatches(BatchSearchRequest searchRequest) {
        Collection<ProductBatchDetailsDTO> result = new ArrayList<>();
        for(SearchServiceAPI provider : getSearchProviders(searchRequest.getDatabases())) {
            result.addAll(provider.searchBatches(searchRequest));
        }
        return result;
    }

    private List<SearchServiceAPI> getSearchProviders(List<String> dataSourceNames) {
        List<SearchServiceAPI> providerList = new ArrayList<>();
        if(dataSourceNames.contains(INDIGO_ELN_DATABASE)) {
            providerList.add(customSearchService);
        } //could be extended for support external data sources

        return providerList;
    }

}
