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

import com.epam.indigoeln.core.repository.search.AbstractSearchAggregationBuilder;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Builds Aggregation For Search.
 */
public final class BatchSearchAggregationBuilder extends AbstractSearchAggregationBuilder {

    private static final String CONTENT_PREFIX = "batches.";
    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("compoundId", "fullNbkBatch",
            "formula", "molWeight.value", "chemicalName", "externalNumber",
            "compoundState.name", "comments", "hazardComments", "casNumber");

    private BatchSearchAggregationBuilder() {
        List<Criteria> match = Arrays.asList(Criteria.where("name").is("productBatchSummary"),
                Criteria.where("name").is("stoichTable"));
        AggregationUtils.orCriteria(match).ifPresent(c -> aggregationOperations.add(Aggregation.match(c)));

        ConditionalOperators.Cond cond = ConditionalOperators.when(Criteria.where("name").is("productBatchSummary"))
                .then("$content.batches")
                .otherwise("$content.products");
        aggregationOperations.add(Aggregation.project().and(cond).as("batches"));

        aggregationOperations.add(Aggregation.unwind("batches")); // unwind (flat array of batches)
        setSearchQueryFields(SEARCH_QUERY_FIELDS);
        setContextPrefix(CONTENT_PREFIX);
    }

    public static BatchSearchAggregationBuilder getInstance() {
        return new BatchSearchAggregationBuilder();
    }

    @Override
    public BatchSearchAggregationBuilder withBingoIds(List<String> bingoIds) {
        if (!bingoIds.isEmpty()) {
            Criteria structureCriteria = Criteria.where(CONTENT_PREFIX + "structure.structureId").in(bingoIds);
            aggregationOperations.add(Aggregation.match(structureCriteria));
        }
        return this;
    }

    public BatchSearchAggregationBuilder search(Optional<String> query, List<SearchCriterion> searchCriterion) {
        if (!searchCriterion.isEmpty()) {
            this.withAdvancedCriteria(searchCriterion);
        }
        query.ifPresent(this::withSearchQuery);
        return this;
    }

    BatchSearchAggregationBuilder withLimit(Optional<Integer> batchesLimit) {
        batchesLimit.ifPresent(limit -> aggregationOperations.add(Aggregation.limit(limit)));
        return this;
    }

    public Aggregation build() {
        return Aggregation.newAggregation(aggregationOperations);
    }
}
