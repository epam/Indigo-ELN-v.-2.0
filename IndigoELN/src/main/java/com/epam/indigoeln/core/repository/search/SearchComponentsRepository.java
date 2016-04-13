package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;

import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
public class SearchComponentsRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest, List<Integer> bingoIds) {
        Aggregation aggregation = buildAggregation(searchRequest, bingoIds);
        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, Component.class, DBObject.class);
        List<DBObject> mappedResults = aggregate.getMappedResults();
        return mappedResults.stream().map(convertResult).collect(Collectors.toList());
    }

    private Aggregation buildAggregation(BatchSearchRequest request, List<Integer> bingoIds) {
        BatchSearchAggregationBuilder builder = BatchSearchAggregationBuilder.getInstance();

        Optional.ofNullable(request.getSearchQuery()).ifPresent(builder::withSearchQuery);

        if(!CollectionUtils.isEmpty(bingoIds)) {
            builder.withBingoIds(bingoIds);
        }

        if(!CollectionUtils.isEmpty(request.getAdvancedSearch())){
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static Function<DBObject, ProductBatchDetailsDTO> convertResult = dbObject -> {
        Map map = dbObject.toMap();
        String fullBatchNumber = map.getOrDefault(BatchComponentUtil.COMPONENT_FIELD_FULL_NBK_BATCH, "").toString();
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    };

}
