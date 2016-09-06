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

    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_THERAPEUTIC_AREA = "therapeuticArea";
    public static final String FIELD_CODE_AND_NAME = "projectCode";
    public static final String FIELD_YIELD = "batchYield";
    public static final String FIELD_PURITY = "purity";
    public static final String FIELD_COMPOUND_ID = "compoundId";
    public static final String FIELD_CHEMICAL_NAME = "chemicalName";
    public static List<String> BATCH_FIELDS = Arrays.asList(FIELD_THERAPEUTIC_AREA, FIELD_CODE_AND_NAME, FIELD_YIELD,
            FIELD_PURITY, FIELD_COMPOUND_ID, FIELD_CHEMICAL_NAME);

    protected List<Aggregation> aggregations;

    public ComponentSearchAggregationBuilder() {
        aggregations = new ArrayList<>();
    }

    public ComponentSearchAggregationBuilder withQuerySearch(String querySearch) {
        aggregations.add(getBatchesAggregation(querySearch));
        aggregations.add(getDescriptionAggregation(querySearch));
        aggregations.add(getConceptAggregation(querySearch));
        return this;
    }

    public ComponentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> criteria) {
        getBatchesAggregation(criteria).ifPresent(aggregations::add);
        getDescriptionAggregation(criteria).ifPresent(aggregations::add);
        getConceptAggregation(criteria).ifPresent(aggregations::add);
        return this;
    }

    public Optional<Collection<Aggregation>> build() {
        return Optional.ofNullable(aggregations.isEmpty() ? null : aggregations);
    }

    private Aggregation getDescriptionAggregation(String querySearch) {
        return getDescriptionAggregation(new SearchCriterion(FIELD_DESCRIPTION, FIELD_DESCRIPTION, "contains", querySearch));
    }

    private Optional<Aggregation> getDescriptionAggregation(List<SearchCriterion> criteria) {
        if (criteria.isEmpty()) {
            return Optional.empty();
        }
        return criteria.stream().filter(c -> FIELD_DESCRIPTION.equals(c.getField())).findAny().map(this::getDescriptionAggregation);
    }

    private Aggregation getBatchesAggregation(String querySearch) {
        Collection<SearchCriterion> searchCriteria = new ArrayList<>();
        searchCriteria.add(new SearchCriterion(FIELD_THERAPEUTIC_AREA, FIELD_THERAPEUTIC_AREA, "contains", querySearch));
        searchCriteria.add(new SearchCriterion(FIELD_CODE_AND_NAME, FIELD_CODE_AND_NAME, "contains", querySearch));
        searchCriteria.add(new SearchCriterion(FIELD_YIELD, FIELD_YIELD, "=", querySearch));
        searchCriteria.add(new SearchCriterion(FIELD_PURITY, FIELD_PURITY, "=", querySearch));
        searchCriteria.add(new SearchCriterion(FIELD_COMPOUND_ID, FIELD_COMPOUND_ID, "contains", querySearch));
        searchCriteria.add(new SearchCriterion(FIELD_CHEMICAL_NAME, FIELD_CHEMICAL_NAME, "contains", querySearch));
        return getBatchesAggregation(searchCriteria, false);
    }

    private Optional<Aggregation> getBatchesAggregation(List<SearchCriterion> criteria) {
        if (criteria.isEmpty()) {
            return Optional.empty();
        }
        final List<SearchCriterion> batchCriteria = criteria.stream().filter(c -> BATCH_FIELDS.contains(c.getField())).collect(Collectors.toList());
        return Optional.ofNullable(batchCriteria.isEmpty() ? null : getBatchesAggregation(batchCriteria, true));

    }

    private Aggregation getConceptAggregation(String querySearch) {
        return getConceptAggregation(new SearchCriterion(FIELD_NAME, FIELD_NAME, "contains", querySearch));
    }

    private Optional<Aggregation> getConceptAggregation(List<SearchCriterion> criteria) {
        if (criteria.isEmpty()) {
            return Optional.empty();
        }
        return criteria.stream().filter(c -> FIELD_NAME.equals(c.getField())).findAny().map(this::getConceptAggregation);
    }

    private Aggregation getDescriptionAggregation(SearchCriterion criterion) {
        List<AggregationOperation> result = new ArrayList<>();
        result.add(Aggregation.project("name", "content"));
        result.add(Aggregation.match(Criteria.where("name").is("experimentDescription")));
        result.add(Aggregation.project(Fields.from(Fields.field("description", "content.description"))));
        result.add(Aggregation.match(AggregationUtils.createCriterion(criterion)));
        return Aggregation.newAggregation(result);
    }

    private Aggregation getBatchesAggregation(Collection<SearchCriterion> criteria, boolean and) {
        List<AggregationOperation> result = new ArrayList<>();
        result.add(Aggregation.project("name", "content"));
        result.add(Aggregation.match(Criteria.where("name").is("productBatchSummary")));
        result.add(Aggregation.project(Fields.from(Fields.field("batches", "content.batches"))));
        result.add(Aggregation.unwind("batches"));
        result.add(Aggregation.project(Fields.from(
                Fields.field(FIELD_THERAPEUTIC_AREA, "batches.therapeuticArea.name"),
                Fields.field(FIELD_CODE_AND_NAME, "batches.codeAndName.name"),
                Fields.field(FIELD_YIELD, "batches.yield"),
                Fields.field(FIELD_PURITY, "batches.purity"),
                Fields.field(FIELD_COMPOUND_ID, "batches.compoundId"),
                Fields.field(FIELD_CHEMICAL_NAME, "batches.chemicalName")
        )));

        Criteria matchCriteria;
        if (and) {
            matchCriteria = new Criteria().andOperator(AggregationUtils.createCriteria(criteria).toArray(new Criteria[criteria.size()]));
        } else {
            matchCriteria = new Criteria().orOperator(AggregationUtils.createCriteria(criteria).toArray(new Criteria[criteria.size()]));
        }
        result.add(Aggregation.match(matchCriteria));

        return Aggregation.newAggregation(result);

    }

    private Aggregation getConceptAggregation(SearchCriterion criterion) {
        List<AggregationOperation> result = new ArrayList<>();
        result.add(Aggregation.project("name", "content"));
        result.add(Aggregation.match(Criteria.where("name").is("conceptDetails")));
        result.add(Aggregation.project(Fields.from(Fields.field(FIELD_NAME, "content.title"))));

        result.add(Aggregation.match(AggregationUtils.createCriterion(criterion)));

        return Aggregation.newAggregation(result);

    }

}
