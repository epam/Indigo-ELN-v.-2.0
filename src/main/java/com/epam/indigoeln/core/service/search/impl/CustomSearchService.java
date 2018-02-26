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
package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.repository.search.component.SearchComponentsRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

/**
 * Implementation of SearchServiceAPI for custom search.
 *
 * @see SearchServiceAPI
 */
@Service
public class CustomSearchService implements SearchServiceAPI {

    private static final String NAME = "Indigo ELN";

    /**
     * SearchComponentsRepository instance for search components.
     */
    @Autowired
    private SearchComponentsRepository searchComponentsRepository;

    /**
     * BingoService instance for molecule search.
     */
    @Autowired
    private BingoService bingoService;

    @Override
    public Info getInfo() {
        return new SearchServiceAPI.Info(1, NAME, true);
    }

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        if (searchRequest.getStructure().isPresent()) {
            List<String> bingoIds = searchByBingoDb(searchRequest.getStructure().get());
            return bingoIds.isEmpty() ? Collections.emptyList()
                    : searchComponentsRepository.findBatches(searchRequest, bingoIds);
        } else {
            return searchComponentsRepository.findBatches(searchRequest, Collections.emptyList());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> searchByBingoDb(BatchSearchStructure structure) {
        List<String> bingoIds;
        String molFile = structure.getMolfile();
        String formula = structure.getFormula();

        switch (structure.getSearchMode()) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchMoleculeSub(molFile, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchMoleculeExact(molFile, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchMoleculeSim(molFile, structure.getSimilarity(), 1f, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(formula, StringUtils.EMPTY);
                break;

            default:
                bingoIds = Collections.emptyList();
        }
        return bingoIds;
    }
}
