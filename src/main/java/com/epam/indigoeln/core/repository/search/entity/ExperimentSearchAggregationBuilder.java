package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class ExperimentSearchAggregationBuilder {

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("name", "status");
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("name", "status");

    private Optional<Collection<Aggregation>> componentsAggregations;

    private List<AggregationOperation> aggregationOperations;

    private ExperimentSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
    }

    public static ExperimentSearchAggregationBuilder getInstance() {
        return new ExperimentSearchAggregationBuilder();
    }

    public Optional<Collection<Aggregation>> getComponentsAggregations() {
        return componentsAggregations;
    }

    public Optional<List<AggregationOperation>> getExperimentAggregationOperations() {
        return Optional.ofNullable(aggregationOperations.isEmpty() ? null : aggregationOperations);
    }

    public ExperimentSearchAggregationBuilder withQuerySearch(String querySearch) {
        //TODO:
        return null;
    }

    public ExperimentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {

        componentsAggregations = new ComponentSearchAggregationBuilder().withAdvancedCriteria(criteria).build();

        List<Criteria> fieldCriteriaList = criteria.stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        Criteria[] mongoCriteriaList = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
        Criteria andCriteria = new Criteria().andOperator(mongoCriteriaList);
        aggregationOperations.add(Aggregation.match(andCriteria));
        return this;
    }

}
