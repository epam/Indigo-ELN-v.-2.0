package com.epam.indigoeln.core.service.search.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.core.integration.BingoResult;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;

import static java.util.stream.Collectors.toList;

@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private BingoDbIntegrationService bingoDbService;

    @Autowired
    private ComponentRepository componentRepository;

    /**
     * Find components (batches) by chemical molecular structure
     * @param structure structure of component
     * @param searchOperator search operator (now, Bingo DB supports 'exact', 'similarity', 'substructure' types)
     * @param options search advanced options
     * @return list of batches with received structure
     */
    @Override
    public Collection<ProductBatchDetailsDTO> searchByMolecularStructure(String structure,
                                                                         String searchOperator,
                                                                         Map options) {
        //search by structure in Bingo DB
        List<Integer> bingoIds =
                handleBingoException(bingoDbService.searchMolecule(structure, searchOperator, options)).getSearchResult();

        if(bingoIds.isEmpty()) {
            return Collections.emptyList();
        }

        //fetch components by bingo db ids
        List<ComponentDTO> components =  componentRepository.findBatchSummariesByBingoDbIds(bingoIds).
                stream().map(ComponentDTO::new).collect(toList());

        //retrieve batches from components
        return BatchComponentUtil.retrieveBatchesByBingoDbId(components, bingoIds).stream().
                map(CustomSearchService::convertFromDBObject).collect(toList());
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

    private BingoResult handleBingoException(BingoResult result) {
        if(!result.isSuccess()) {
            throw new CustomParametrizedException(result.getErrorMessage());
        }
        return result;
    }

    private static ProductBatchDetailsDTO convertFromDBObject(BasicDBObject obj) {
        Map map = obj.toMap();
        String fullBatchNumber = Optional.ofNullable(map.get(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH)).
                map(Object::toString).orElse(null);
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    }
}
