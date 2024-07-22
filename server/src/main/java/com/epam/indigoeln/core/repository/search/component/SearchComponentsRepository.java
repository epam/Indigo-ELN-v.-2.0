/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        Map map = (Map) dbObject.toMap().getOrDefault("batches", Collections.emptyMap());
        String fullBatchNumber = map.getOrDefault(BatchComponentUtil.COMPONENT_FIELD_NBK_BATCH, "")
                .toString();
        return new ProductBatchDetailsDTO(fullBatchNumber, map);
    };
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SearchComponentsRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest, List<String> bingoIds) {
        Aggregation aggregation = buildAggregation(searchRequest, bingoIds);
        LOGGER.debug("Perform search query: " + aggregation.toString());

        AggregationResults<DBObject> aggregate = mongoTemplate.aggregate(aggregation, Component.class, DBObject.class);
        List<DBObject> mappedResults = aggregate.getMappedResults();
        return mappedResults.stream().map(convertResult).collect(Collectors.toList());
    }

    private Aggregation buildAggregation(BatchSearchRequest request, List<String> bingoIds) {
        BatchSearchAggregationBuilder builder = BatchSearchAggregationBuilder.getInstance();
        return builder.search(request.getSearchQuery(), request.getAdvancedSearch())
                .withBingoIds(bingoIds)
                .withLimit(request.getBatchesLimit())
                .build();
    }
}
