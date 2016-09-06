package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ExperimentSearchAggregationBuilder {

    private static final List<String> SEARCH_QUERY_FIELDS = Collections.singletonList("status");
    private static final Collection<String> AVAILABLE_FIELDS = Collections.singletonList("status");

    private MongoTemplate template;

    private List<AggregationOperation> aggregationOperations;

    private ExperimentSearchAggregationBuilder(MongoTemplate template) {
        this.template = template;
        aggregationOperations = new ArrayList<>();
        aggregationOperations.add(Aggregation.unwind("components"));
    }

    public static ExperimentSearchAggregationBuilder getInstance(MongoTemplate template) {
        return new ExperimentSearchAggregationBuilder(template);
    }

    public ExperimentSearchAggregationBuilder withQuerySearch(String querySearch) {

        Optional<Collection<Aggregation>> componentsAggregations = new ComponentSearchAggregationBuilder().withQuerySearch(querySearch).build();
        final Optional<Set<DBRef>> dbRefs = componentsAggregations.map(ca -> getDBRefs(ca, false));

        List<Criteria> fieldCriteriaList = SEARCH_QUERY_FIELDS.stream().map(
                field -> Criteria.where(field).regex(".*" + querySearch + ".*")).
                collect(toList());
        dbRefs.ifPresent(refs -> {
            fieldCriteriaList.add(Criteria.where("components").in(refs));
        });

        Criteria[] fieldCriteriaArr = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
        Criteria orCriteria = new Criteria().orOperator(fieldCriteriaArr);
        aggregationOperations.add(Aggregation.match(orCriteria));

        return this;
    }

    public ExperimentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {

        Optional<Collection<Aggregation>> componentsAggregations = new ComponentSearchAggregationBuilder().withAdvancedCriteria(criteria).build();
        final Optional<Set<DBRef>> dbRefs = componentsAggregations.map(ca -> getDBRefs(ca, true));

        List<Criteria> fieldCriteriaList = criteria.stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        dbRefs.ifPresent(refs -> {
            fieldCriteriaList.add(Criteria.where("components").in(refs));
        });
        if (!fieldCriteriaList.isEmpty()) {
            Criteria[] mongoCriteriaList = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
            Criteria andCriteria = new Criteria().andOperator(mongoCriteriaList);
            aggregationOperations.add(Aggregation.match(andCriteria));
        }
        return this;
    }

    public Optional<Aggregation> build() {
        return Optional.ofNullable(aggregationOperations.isEmpty() ? null : Aggregation.newAggregation(aggregationOperations));
    }

    private Set<DBRef> getDBRefs(Collection<Aggregation> componentsAggregations, boolean and) {
        final List<Set<DBRef>> component = componentsAggregations.stream()
                .map(aggregation ->
                        template.aggregate(aggregation, Component.class, Component.class).getMappedResults()
                                .stream().map(c -> new DBRef("component", new ObjectId(c.getId()))).collect(Collectors.toSet()))
                .collect(Collectors.toList());
        Set<DBRef> result = new HashSet<>();
        if (component.isEmpty()) {
            return result;
        }
        result.addAll(component.get(0));
        for (int i = 1; i < component.size(); i++) {
            if (and) {
                result.retainAll(component.get(i));
            } else {
                result.addAll(component.get(i));
            }
        }
        return result;
    }

}
