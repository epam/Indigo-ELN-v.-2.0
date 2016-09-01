package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

public class ComponentSearchAggregationBuilder {

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_THERAPEUTIC_AREA = "therapeuticArea";
    public static final String FIELD_CODE_AND_NAME = "codeAndName";
    public static final String FIELD_YIELD = "yield";
    public static final String FIELD_PURITY = "purity";
    public static final String FIELD_COMPOUND_ID = "compoundId";
    public static final String FIELD_CHEMICAL_NAME = "chemicalName";
    public static List<String> BATCH_FIELDS = Arrays.asList(FIELD_THERAPEUTIC_AREA, FIELD_CODE_AND_NAME, FIELD_YIELD,
            FIELD_PURITY, FIELD_COMPOUND_ID, FIELD_CHEMICAL_NAME);

    protected List<Aggregation> aggregations;

    public ComponentSearchAggregationBuilder() {
        aggregations = new ArrayList<>();
    }

    public ComponentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {
        getBatchesAggregation(criteria).ifPresent(aggregations::add);
        getDescriptionAggregation(criteria).ifPresent(aggregations::add);
        return this;
    }

    public Optional<Collection<Aggregation>> build() {
        return Optional.ofNullable(aggregations.isEmpty() ? null : aggregations);
    }

    private Optional<Aggregation> getDescriptionAggregation(List<SearchCriterion> criteria) {
        if (criteria.isEmpty()) {
            return Optional.empty();
        }
        return criteria.stream().filter(c -> FIELD_DESCRIPTION.equals(c.getField())).findAny().map(this::getDescriptionAggregation);
    }

    private Optional<Aggregation> getBatchesAggregation(List<SearchCriterion> criteria) {
        if (criteria.isEmpty()) {
            return Optional.empty();
        }
        final List<SearchCriterion> batchCriteria = criteria.stream().filter(c -> BATCH_FIELDS.contains(c.getField())).collect(Collectors.toList());
        return getBatchesAggregation(batchCriteria);
    }

    private Aggregation getDescriptionAggregation(SearchCriterion criterion) {
        List<AggregationOperation> result = new ArrayList<>();
        result.add(Aggregation.project("name", "content"));
        result.add(Aggregation.match(Criteria.where("name").is("experimentDescription")));
        result.add(Aggregation.project(Fields.from(Fields.field("description", "content.description"))));
        result.add(Aggregation.match(AggregationUtils.createCriterion(criterion)));
        return Aggregation.newAggregation(result);
    }

    private Aggregation getBatchesAggregation(Collection<SearchCriterion> criteria) {
        List<AggregationOperation> result = new ArrayList<>();
        result.add(Aggregation.project("name", "content"));
        result.add(Aggregation.match(Criteria.where("name").is("productBatchSummary")));
        result.add(Aggregation.project(Fields.from(Fields.field("batches", "content.batches"))));
        result.add(Aggregation.unwind("batches"));
        result.add(Aggregation.project(Fields.from(
                Fields.field("therapeuticArea", "batches.therapeuticArea.name"),
                Fields.field("codeAndName", "batches.codeAndName.name"),
                Fields.field("yield", "batches.yield"),
                Fields.field("purity", "batches.purity"),
                Fields.field("compoundId", "batches.compoundId"),
                Fields.field("chemicalName", "batches.chemicalName")
        )));

        result.add(Aggregation.match(AggregationUtils.createCriteria(criteria)));

        return Aggregation.newAggregation(result);

    }

}
