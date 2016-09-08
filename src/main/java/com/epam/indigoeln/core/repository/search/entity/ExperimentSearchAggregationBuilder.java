package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class ExperimentSearchAggregationBuilder {

    public static final String FIELD_STATUS = "status";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMPONENTS = "components";
    public static final String FIELD_EXPERIMENT_VERSION = "experimentVersion";
    public static final String FIELD_CREATION_DATE = "creationDate";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_ACCESS_LIST = "accessList";

    private static final List<String> SEARCH_QUERY_FIELDS = Collections.singletonList(FIELD_STATUS);
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList(FIELD_STATUS, "author._id");

    private MongoTemplate template;

    private Collection<AggregationOperation> baseOperations;
    private Optional<AggregationOperation> experimentFilter;

    private ExperimentSearchAggregationBuilder(MongoTemplate template) {
        this.template = template;
        baseOperations = new ArrayList<>();
        baseOperations.add(sort(Sort.Direction.ASC, FIELD_NAME, FIELD_EXPERIMENT_VERSION));
        baseOperations.add(group(FIELD_NAME)
                .last(FIELD_COMPONENTS).as(FIELD_COMPONENTS)
                .last(FIELD_STATUS).as(FIELD_STATUS)
                .last(FIELD_AUTHOR).as(FIELD_AUTHOR)
                .last(FIELD_NAME).as(FIELD_NAME)
                .last(FIELD_CREATION_DATE).as(FIELD_CREATION_DATE)
                .last(FIELD_ACCESS_LIST).as(FIELD_ACCESS_LIST));
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
        dbRefs.map(Criteria.where(FIELD_COMPONENTS)::in).ifPresent(cr -> {
            baseOperations.add(unwind(FIELD_COMPONENTS));
            fieldCriteriaList.add(cr);
        });
        if (fieldCriteriaList.isEmpty()) {
            experimentFilter = Optional.empty();
        } else {
            Criteria[] fieldCriteriaArr = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
            experimentFilter = Optional.of(match(new Criteria().orOperator(fieldCriteriaArr)));
        }
        return this;
    }

    public ExperimentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {

        Optional<Collection<Aggregation>> componentsAggregations = new ComponentSearchAggregationBuilder().withAdvancedCriteria(criteria).build();
        final Optional<Set<DBRef>> dbRefs = componentsAggregations.map(ca -> getDBRefs(ca, true));

        List<Criteria> fieldCriteriaList = criteria.stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        dbRefs.map(Criteria.where(FIELD_COMPONENTS)::in).ifPresent(cr -> {
            baseOperations.add(unwind(FIELD_COMPONENTS));
            fieldCriteriaList.add(cr);
        });
        if (fieldCriteriaList.isEmpty()) {
            experimentFilter = Optional.empty();
        } else {
            Criteria[] fieldCriteriaArr = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
            experimentFilter = Optional.of(match(new Criteria().andOperator(fieldCriteriaArr)));
        }
        return this;
    }

    public Optional<Aggregation> build() {
        return experimentFilter.map(ef -> {
            List<AggregationOperation> operations = new ArrayList<>(baseOperations);
            operations.add(ef);
            operations.add(group(FIELD_NAME)
                    .last(FIELD_STATUS).as(FIELD_STATUS)
                    .last(FIELD_AUTHOR).as(FIELD_AUTHOR)
                    .last(FIELD_NAME).as(FIELD_NAME)
                    .last(FIELD_CREATION_DATE).as(FIELD_CREATION_DATE)
                    .last(FIELD_ACCESS_LIST).as(FIELD_ACCESS_LIST));
            return Aggregation.newAggregation(operations);
        });
    }

    private Set<DBRef> getDBRefs(Collection<Aggregation> componentsAggregations, boolean and) {
        return collect(componentsAggregations.stream()
                .map(aggregation ->
                        template.aggregate(aggregation, Component.class, Component.class).getMappedResults()
                                .stream().map(c -> new DBRef("component", new ObjectId(c.getId()))).collect(Collectors.toSet()))
                .collect(Collectors.toList()), and);
    }

    private Set<DBRef> collect(List<Set<DBRef>> refs, boolean and) {
        Set<DBRef> result = new HashSet<>();
        if (refs.isEmpty()) {
            return result;
        }
        result.addAll(refs.get(0));
        for (int i = 1; i < refs.size(); i++) {
            if (and) {
                result.retainAll(refs.get(i));
            } else {
                result.addAll(refs.get(i));
            }
        }
        return result;
    }

}
