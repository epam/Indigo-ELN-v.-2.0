package com.epam.indigoeln.core.service.search.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.epam.indigoeln.core.service.bingo.BingoService;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.util.ComponentsUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.util.ComponentsUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

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

        List<String> bingoIds;

        switch (searchOperator) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchMoleculeSub(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchMoleculeExact(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchMoleculeSim(structure, Float.valueOf(options.get("min").toString()), Float.valueOf(options.get("max").toString()), StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            default:
                bingoIds = new ArrayList<>();
                break;
        }

        return bingoIds.isEmpty() ? Collections.emptyList() : componentRepository.findBatchesByBingoDbIds(bingoIds).stream().map(ComponentDTO::new).collect(toList());
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
     * Find component by full batch number
     * Full batch number expected in format NOTEBOOK_NUMBER(8 digits)-EXPERIMENT_NUMBER(4 digits)-BATCH_NUMBER(3 digits)
     *
     * Find product batch details by full batch number
     *
     * @param fullBatchNumber full batch number
     * @return result of search
     */
    @Override
    public Optional<ProductBatchDetailsDTO> searchByNotebookBatchNumber(String fullBatchNumber) {
        Optional<Component> batchSummaryComponent = componentRepository.findBatchSummaryByFullBatchNumber(fullBatchNumber);
        if(!batchSummaryComponent.isPresent()) {

        ComponentDTO batchSummaryComponentDTO = new ComponentDTO(batchSummaryComponent.get());
        return BatchComponentUtil.retrieveBatchByNumber(Collections.singletonList(batchSummaryComponentDTO), fullBatchNumber).
                map(params -> new ProductBatchDetailsDTO(fullBatchNumber, params));
    }

    private static ProductBatchDetailsDTO convertFromDBObject(BasicDBObject obj) {
        Map map = obj.toMap();
        String fullBatchNumber = Optional.ofNullable(map.get(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH)).
                map(Object::toString).orElse(null);
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    }

}
