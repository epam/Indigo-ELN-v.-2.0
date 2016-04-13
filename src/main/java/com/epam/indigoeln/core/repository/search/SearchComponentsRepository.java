package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;

import com.google.common.collect.ImmutableMap;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Repository
public class SearchComponentsRepository {

    @Autowired
    private BingoService bingoService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        Aggregation aggregation = buildAggregation(searchRequest);
        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, Component.class, DBObject.class);
        List<DBObject> mappedResults = aggregate.getMappedResults();
        return mappedResults.stream().map(convertResult).collect(Collectors.toList());
    }

    private Aggregation buildAggregation(BatchSearchRequest request) {
        BatchSearchAggregationBuilder builder = BatchSearchAggregationBuilder.getInstance();

        Optional.ofNullable(request.getSearchQuery()).ifPresent(builder::withSearchQuery);

        List<Integer> bingoIds = getBingoDbIds(request);
        if(!CollectionUtils.isEmpty(bingoIds)) {
            builder.withBingoIds(bingoIds);
        }

        if(!CollectionUtils.isEmpty(request.getAdvancedSearch())){
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        return builder.build();
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
                        Float.valueOf(options.getOrDefault("max", 100f).toString()), StringUtils.EMPTY);
                break;
            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(structure, StringUtils.EMPTY);
                break;
            default:
                bingoIds = Collections.emptyList();
        }
        return bingoIds;
    }

    @SuppressWarnings("unchecked")
    private static Function<DBObject, ProductBatchDetailsDTO> convertResult = dbObject -> {
        Map map = dbObject.toMap();
        String fullBatchNumber = map.getOrDefault(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH, "").toString();
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    };

}
