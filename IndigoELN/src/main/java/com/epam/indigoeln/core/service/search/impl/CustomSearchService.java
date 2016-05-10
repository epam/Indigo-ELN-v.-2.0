package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.repository.search.SearchComponentsRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;


@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private SearchComponentsRepository searchComponentsRepository;

    @Autowired
    private BingoService bingoService;

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        List<Integer> bingoIds;
        if(searchRequest.getStructure() != null) {
            bingoIds = getBingoDbIds(searchRequest);
            if(bingoIds.isEmpty()) {
                return Collections.emptyList();
            }
        } else {
            bingoIds = Collections.emptyList();
        }

        return searchComponentsRepository.findBatches(searchRequest, bingoIds);
    }

    private List<Integer> getBingoDbIds(BatchSearchRequest searchRequest) {
        if(searchRequest.getStructure() == null) {
            return Collections.emptyList();
        }

        BatchSearchStructure structure = searchRequest.getStructure();
        List<Integer> result;
        if(structure.getFormula()!= null) {
            result = searchByBingoDb(structure.getFormula(), CHEMISTRY_SEARCH_MOLFORMULA, Collections.emptyMap());
        } else {
            Map options = ImmutableMap.of("min", structure.getSimilarity());
            result = searchByBingoDb(structure.getMolfile(), structure.getSearchMode(), options);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> searchByBingoDb(String structure, String searchOperator, Map options) {
        List<Integer> bingoIds;

        switch (searchOperator) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchMoleculeSub(structure, StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchMoleculeExact(structure, StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchMoleculeSim(structure,
                        Float.valueOf(options.getOrDefault("min", 0f).toString()),
                        Float.valueOf(options.getOrDefault("max", 1f).toString()), StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(structure, StringUtils.EMPTY);
                break;
            default:
                bingoIds = Collections.emptyList();
        }
        return bingoIds;
    }
}
