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


@Service
public class CustomSearchService implements SearchServiceAPI {

    private static final String NAME = "Indigo ELN";

    @Autowired
    private SearchComponentsRepository searchComponentsRepository;

    @Autowired
    private BingoService bingoService;

    @Override
    public Info getInfo() {
        return new SearchServiceAPI.Info(1, NAME, true);
    }

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        if(searchRequest.getStructure().isPresent()) {
            List<Integer> bingoIds = searchByBingoDb(searchRequest.getStructure().get());
            return bingoIds.isEmpty() ? Collections.emptyList() :
                    searchComponentsRepository.findBatches(searchRequest, bingoIds);
        } else {
            return searchComponentsRepository.findBatches(searchRequest, Collections.emptyList());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Integer> searchByBingoDb(BatchSearchStructure structure) {
        List<Integer> bingoIds;
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
