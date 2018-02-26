/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides methods for search.
 */
@Service
public class SearchServiceFacade {

    /**
     * List of catalogues.
     */
    @Autowired
    private List<SearchServiceAPI> catalogues;

    public Collection<SearchServiceAPI.Info> getCatalogues() {
        return catalogues.stream().map(SearchServiceAPI::getInfo).collect(Collectors.toList());
    }

    /**
     * Returns list with product batch details transfer objects.
     *
     * @param searchRequest Search request
     * @return List with product batch details transfer objects
     */
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        Collection<ProductBatchDetailsDTO> result = new ArrayList<>();
        for (SearchServiceAPI provider : getSearchProviders(searchRequest.getDatabases())) {
            Collection<ProductBatchDetailsDTO> batches = provider.findBatches(searchRequest);
            result.addAll(batches);
            Optional<Integer> batchesLeft = searchRequest.getBatchesLimit();
            if (batchesLeft.isPresent()) {
                if (batches.size() == batchesLeft.get()) {
                    break;
                } else {
                    searchRequest.setBatchesLimit(batchesLeft.get() - batches.size());
                }
            }
        }
        return result;
    }

    private Collection<SearchServiceAPI> getSearchProviders(List<String> dataSourceNames) {
        return catalogues.stream().filter(p -> dataSourceNames.contains(p.getInfo().getValue()))
                .collect(Collectors.toList());
    }
}
