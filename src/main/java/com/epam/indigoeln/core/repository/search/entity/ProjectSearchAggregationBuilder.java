package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AbstractSearchAggregationBuilder;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProjectSearchAggregationBuilder extends AbstractSearchAggregationBuilder {

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("description", "name", "keywords", "references");
    private static final List<String> AVAILABLE_FIELDS = Arrays.asList("description", "name", "keywords", "references", "author._id");

    private ProjectSearchAggregationBuilder() {
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
