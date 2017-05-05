package com.epam.indigoeln.core.repository.search.component;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;

import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
public class SearchComponentsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchComponentsRepository.class);
    @SuppressWarnings("unchecked")
    private static Function<DBObject, ProductBatchDetailsDTO> convertResult = dbObject -> {
        Map content = (Map) dbObject.toMap().getOrDefault("content", Collections.emptyMap());
        Map map = (Map) content.getOrDefault("batches", Collections.emptyMap());

        String fullBatchNumber = map.getOrDefault(BatchComponentUtil.COMPONENT_FIELD_NBK_BATCH, "").toString();
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    };
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest, List<String> bingoIds) {
        Aggregation aggregation = buildAggregation(searchRequest, bingoIds);
        LOGGER.debug("Perform search query: " + aggregation.toString());

        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, Component.class, DBObject.class);
        List<DBObject> mappedResults = aggregate.getMappedResults();
        return mappedResults.stream().map(convertResult).collect(Collectors.toList());
    }

    private Aggregation buildAggregation(BatchSearchRequest request, List<String> bingoIds) {
        BatchSearchAggregationBuilder builder = BatchSearchAggregationBuilder.getInstance();
        request.getSearchQuery().ifPresent(builder::withSearchQuery);

        if(!bingoIds.isEmpty()) {
            builder.withBingoIds(bingoIds);
        }

        if(!request.getAdvancedSearch().isEmpty()) {
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        request.getBatchesLimit().ifPresent(builder::withLimit);

        return builder.build();
    }
}
