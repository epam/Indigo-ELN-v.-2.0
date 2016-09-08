package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.search.entity.EntitySearchRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchStructure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Service
public class EntitySearchService {

    @Autowired
    private EntitySearchRepository repository;

    @Autowired
    private BingoService bingoService;

    public List<EntitySearchResultDTO> find(User user, EntitySearchRequest searchRequest) {
        final List<Integer> bingoIds = searchRequest.getStructure().map(this::searchByBingoDb).orElse(Collections.emptyList());
        return repository.findEntities(user, searchRequest, bingoIds);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> searchByBingoDb(EntitySearchStructure structure) {
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
