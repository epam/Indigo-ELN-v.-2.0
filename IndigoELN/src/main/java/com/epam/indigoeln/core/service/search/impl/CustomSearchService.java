package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.repository.search.SearchComponentsRepository;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;

import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private SearchComponentsRepository searchComponentsRepository;

    @Override
    public Collection<ProductBatchDetailsDTO> searchBatches(BatchSearchRequestDTO searchRequest) {
        return searchComponentsRepository.findBatches(searchRequest);
    }




}
