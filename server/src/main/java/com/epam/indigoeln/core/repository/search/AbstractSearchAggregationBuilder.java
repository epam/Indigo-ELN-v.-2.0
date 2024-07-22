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
package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class AbstractSearchAggregationBuilder {

    protected List<AggregationOperation> aggregationOperations;
    private Collection<String> searchQueryFields;
    private Set<String> advancedFields = new HashSet<>();
    private String contextPrefix = "";

    protected AbstractSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
    }

    public AbstractSearchAggregationBuilder withBingoIds(List<String> bingoIds) {
        throw new UnsupportedOperationException("This method should be implemented in subclasses");
    }

    protected AbstractSearchAggregationBuilder withSearchQuery(String searchQuery) {
        if (searchQueryFields != null) {
            List<Criteria> querySearch = searchQueryFields.stream()
                    .filter(f -> !advancedFields.contains(f))
                    .map(f -> new SearchCriterion(f, f, "contains", searchQuery))
                    .map(c -> AggregationUtils.createCriterion(c, contextPrefix))
                    .collect(toList());
            AggregationUtils.orCriteria(querySearch).ifPresent(c -> aggregationOperations.add(Aggregation.match(c)));
        }
        return this;
    }

    protected AbstractSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {
        List<Criteria> advancedSearch = criteria.stream()
                .map(criterion -> {
                    advancedFields.add(criterion.getField());
                    return AggregationUtils.createCriterion(criterion, contextPrefix);
                })
                .collect(toList());

        AggregationUtils.andCriteria(advancedSearch).ifPresent(c -> aggregationOperations.add(Aggregation.match(c)));
        return this;
    }

    protected void setContextPrefix(String contextPrefix) {
        this.contextPrefix = contextPrefix;
    }

    protected void setSearchQueryFields(Collection<String> searchQueryFields) {
        this.searchQueryFields = searchQueryFields;
    }
}
