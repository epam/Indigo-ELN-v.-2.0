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

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.search.entity.EntitySearchRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.util.AuthoritiesUtil;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchStructure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

/**
 * Provides methods for search entity.
 */
@Service
public class EntitySearchService {

    /**
     * EntitySearchRepository instance for entity search.
     */
    @Autowired
    private EntitySearchRepository repository;

    /**
     * BingoService instance for molecule search.
     */
    @Autowired
    private BingoService bingoService;

    /**
     * Searches entity by search request.
     *
     * @param user          Current user
     * @param searchRequest Search request
     * @return Entities which were found
     */
    public List<EntitySearchResultDTO> find(User user, EntitySearchRequest searchRequest) {
        List<String> bingoIds;
        Optional<EntitySearchStructure> structure = searchRequest.getStructure();
        if (structure.isPresent() && AuthoritiesUtil.canReadExperiment(user)) {
            bingoIds = searchByBingoDb(structure.get());
        } else {
            bingoIds = Collections.emptyList();
        }
        return repository.findEntities(user, searchRequest, bingoIds);
    }

    @SuppressWarnings("unchecked")
    private List<String> searchByBingoDb(EntitySearchStructure structure) {
        switch (structure.getType().getName()) {
            case PRODUCT:
                return searchMoleculeByBingoDb(structure);
            case REACTION:
                return searchReactionByBingoDb(structure);
            default:
                return Collections.emptyList();
        }
    }

    private List<String> searchReactionByBingoDb(EntitySearchStructure structure) {
        List<String> bingoIds;
        String molFile = structure.getMolfile();
        String formula = structure.getFormula();

        switch (structure.getSearchMode()) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchReactionSub(molFile, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchReactionExact(molFile, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchReactionSim(molFile, structure.getSimilarity(),
                        1f, StringUtils.EMPTY);
                break;

            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchReactionMolFormula(formula, StringUtils.EMPTY);
                break;

            default:
                bingoIds = Collections.emptyList();
        }
        return bingoIds;
    }

    private List<String> searchMoleculeByBingoDb(EntitySearchStructure structure) {
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
                bingoIds = bingoService.searchMoleculeSim(molFile,
                        structure.getSimilarity(), 1f, StringUtils.EMPTY);
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
