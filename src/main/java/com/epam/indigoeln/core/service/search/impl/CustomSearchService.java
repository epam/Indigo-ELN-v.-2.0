package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;

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

    /**
     * Find components (batches) by chemical molecular structure
     *
     * @param structure      structure of component
     * @param searchOperator search operator (now, Bingo DB supports 'exact', 'similarity', 'substructure' types)
     * @param options        search advanced options
     * @return list of batches with received structure
     */
    @Override
    public Collection<ProductBatchDetailsDTO> searchByMolecularStructure(String structure,
                                                                         String searchOperator,
                                                                         Map options) {

        // TODO Need to replace StringUtils.EMPTY with correct options for Bingo
        List<Integer> bingoIds;

        switch (searchOperator) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchMoleculeSub(structure, StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchMoleculeExact(structure, StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchMoleculeSim(structure, Float.valueOf(options.get("min").toString()), Float.valueOf(options.get("max").toString()), StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(structure, StringUtils.EMPTY);
                break;
            default:
                bingoIds = new ArrayList<>();
                break;
        }

        //fetch components by bingo db ids
        List<ComponentDTO> components =  componentRepository.findBatchSummariesByBingoDbIds(bingoIds).
                stream().map(ComponentDTO::new).collect(toList());

        //retrieve batches from components
        return BatchComponentUtil.retrieveBatchesByBingoDbId(components, bingoIds).stream().
                map(this::convertFromDBObject).collect(toList());
    }

    /**
     * Find product batch details by full batch number
     *
     * @param fullBatchNumber full batch number
     * @return result of search
     */
    @Override
    public Optional<ProductBatchDetailsDTO> searchByNotebookBatchNumber(String fullBatchNumber) {
        Optional<Component> batchSummaryComponent = componentRepository.findBatchSummaryByFullBatchNumber(fullBatchNumber);
        if(!batchSummaryComponent.isPresent()) {
            return Optional.empty();
        }

        ComponentDTO batchSummaryComponentDTO = new ComponentDTO(batchSummaryComponent.get());
        return BatchComponentUtil.retrieveBatchByNumber(Collections.singletonList(batchSummaryComponentDTO), fullBatchNumber).
                map(params -> new ProductBatchDetailsDTO(fullBatchNumber, params));
    }

    public ProductBatchDetailsDTO convertFromDBObject(BasicDBObject obj) {
        Map map = obj.toMap();
        String fullBatchNumber = Optional.ofNullable(map.get(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH)).
                map(Object::toString).orElse(null);
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    }

}
