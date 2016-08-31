package com.epam.indigoeln.core.repository.search;

import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProjectSearchAggregationBuilder extends AbstractSearchAggregationBuilder {

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("description", "name");
    private static final List<String> AVAILABLE_FIELDS = Arrays.asList("creationDate", "description", "name");

    private ProjectSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
        setSearchQueryFields(SEARCH_QUERY_FIELDS);
        setAvailableFields(AVAILABLE_FIELDS);
    }

    public static ProjectSearchAggregationBuilder getInstance() {
        return new ProjectSearchAggregationBuilder();
    }

    public Optional<Aggregation> build() {
        return Optional.ofNullable(aggregationOperations.isEmpty() ? null : Aggregation.newAggregation(aggregationOperations));
    }

}
