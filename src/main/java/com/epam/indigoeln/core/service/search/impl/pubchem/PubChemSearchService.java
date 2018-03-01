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
package com.epam.indigoeln.core.service.search.impl.pubchem;

import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

/**
 * Implementation of SearchServiceAPI for search product batch details.
 *
 * @see SearchServiceAPI
 */
@Service
public class PubChemSearchService implements SearchServiceAPI {

    private static final String NAME = "PubChem";

    /**
     * PubChemRepository instance to searching.
     */
    @Autowired
    private PubChemRepository pubChemRepository;

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        if (searchRequest.getStructure().isPresent()) {
            return search(searchRequest);
        } else {
            return Collections.emptyList();
        }
    }

    private Collection<ProductBatchDetailsDTO> search(BatchSearchRequest searchRequest) {
        Collection<ProductBatchDetailsDTO> result;
        switch (searchRequest.getStructure().get().getSearchMode()) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                result = pubChemRepository.searchBySubStructure(searchRequest);
                break;

            case CHEMISTRY_SEARCH_EXACT:
                result = pubChemRepository.searchByIdentity(searchRequest);
                break;

            case CHEMISTRY_SEARCH_SIMILARITY:
                result = pubChemRepository.searchBySimilarity(searchRequest);
                break;

            case CHEMISTRY_SEARCH_MOLFORMULA:
                result = pubChemRepository.searchByFormula(searchRequest);
                break;

            default:
                result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public Info getInfo() {
        return new SearchServiceAPI.Info(2, NAME, true);
    }
}
