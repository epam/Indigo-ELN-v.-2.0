package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.google.common.collect.ImmutableMap;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_EXACT;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_MOLFORMULA;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE;

//db.component.aggregate({$match: {'name' :'productBatchSummary'}}, {$unwind : "$content.batches"}, {$match: {"content.batches.molFormula" : "C6 H6"}})
@Repository
public class SearchComponentsRepository {

    @Autowired
    private BingoService bingoService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        Aggregation aggregation = buildAggregation(searchRequest);
        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, Component.class, DBObject.class);
        return aggregate.getMappedResults().stream().map(
                SearchComponentsRepository::convertResult
        ).collect(Collectors.toList());
    }

    private Aggregation buildAggregation(BatchSearchRequest searchRequest) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        aggregationOperations.add(Aggregation.match(Criteria.where("name").is("productBatchSummary"))); // filter by type
        aggregationOperations.add(Aggregation.project("content"));
        aggregationOperations.add(Aggregation.unwind("content.batches")); // unwind (flat array of batches)

        Optional.ofNullable(searchRequest.getStructure()).ifPresent(
           structure -> {
               List<Integer> bingoIds;
               if(structure.getFormula()!= null) {
                   bingoIds = searchByBingoDb(structure.getFormula(), CHEMISTRY_SEARCH_MOLFORMULA, Collections.emptyMap());
               } else {
                   Map options = ImmutableMap.of("min", structure.getSimilarity());
                   bingoIds = searchByBingoDb(structure.getMolfile(), structure.getSearchMode(), options);
               }

               if(!bingoIds.isEmpty()) {
                   Criteria structureCriteria = Criteria.where("structure.structureId").in(bingoIds);
                   aggregationOperations.add(Aggregation.match(structureCriteria));
               }

           });

        if(!CollectionUtils.isEmpty(searchRequest.getAdvancedSearch())) { //if attribute parameters are specified
            List<Criteria> fieldCriteriaList = searchRequest.getAdvancedSearch().stream().
                    map(BatchCriteriaConverter::convert).collect(Collectors.toList());
            Criteria[] criteriaList = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
            Criteria andCriteria = new Criteria().andOperator(criteriaList);
            aggregationOperations.add(Aggregation.match(andCriteria));
        }

        return Aggregation.newAggregation(aggregationOperations);
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
                bingoIds = new ArrayList<>();
                break;
        } return bingoIds;
    }

    private static ProductBatchDetailsDTO convertResult(DBObject component) {
        Map map = component.toMap();
        String fullBatchNumber = Optional.ofNullable(map.get(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH)).
                map(Object::toString).orElse(null);
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    }
}
