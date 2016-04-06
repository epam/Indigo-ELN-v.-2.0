package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;

import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequestDTO;
import com.mongodb.BasicDBObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_EXACT;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_MOLFORMULA;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE;
import static java.util.stream.Collectors.toList;

@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private BingoService bingoService;


    @Override
    public Collection<ProductBatchDetailsDTO> searchByMolecularFormula(String formula,
                                                                       Map options) {

        // TODO Need to replace StringUtils.EMPTY with correct options for Bingo
        List<Integer> bingoIds = searchByBingoDb(formula, CHEMISTRY_SEARCH_MOLFORMULA, options);

        //fetch components by bingo db ids
        List<ComponentDTO> components = componentRepository.findBatchSummariesByBingoDbIds(bingoIds).
                stream().map(ComponentDTO::new).collect(toList());

        //retrieve batches from components
        return BatchComponentUtil.retrieveBatchesByBingoDbId(components, bingoIds).stream().
                map(this::convertFromDBObject).collect(toList());
    }


    @Override
    public Collection<ProductBatchDetailsDTO> searchBatches(BatchSearchRequestDTO searchRequest) {
        return Collections.emptyList();
    }

    private ProductBatchDetailsDTO convertFromDBObject(BasicDBObject obj) {
        Map map = obj.toMap();
        String fullBatchNumber = Optional.ofNullable(map.get(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH)).
                map(Object::toString).orElse(null);
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
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
                bingoIds = new ArrayList<>();
                break;
        } return bingoIds;
    }
}
