package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchCriteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Builds Aggregation For Search
 *
 * Mongo DB Example :
 *
 * db.component.aggregate(
 *      {$match: {'name' :'productBatchSummary'}},
 *      {$project: {$content}},
 *      {$unwind : "$content.batches"},
 *      {$match: {"content.batches.molFormula" : "C6 H6"}}
 * )
 *
 */
public final class BatchSearchAggregationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSearchAggregationBuilder.class);

    private static final String CONTENT_PREFIX = "content.batches.";
    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("nbkBatch", "molFormula", "molWeight",
            "chemicalName", "externalNumber", "compoundState", "comments", "hazardComments", "casNumber");
    private List<AggregationOperation> aggregationOperations;

    private BatchSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
        aggregationOperations.add(Aggregation.match(Criteria.where("name").is("productBatchSummary"))); // filter by type
        aggregationOperations.add(Aggregation.project("content"));
        aggregationOperations.add(Aggregation.unwind("content.batches")); // unwind (flat array of batches)
    }

    public static BatchSearchAggregationBuilder getInstance() {
        return new BatchSearchAggregationBuilder();
    }

    private static Criteria convertCriteria(BatchSearchCriteria dto) {
        Criteria criteria = Criteria.where(CONTENT_PREFIX + dto.getField());
        Object value = dto.getValue();
        switch (dto.getCondition()) {
            case "contains":
                criteria.regex(".*" + value + ".*");
                break;
            case "starts with":
                criteria.regex(value + ".*");
                break;
            case "ends with":
                criteria.regex(".*" + value);
                break;
            case "=":
                criteria.is(convertToDouble(value));
                break;
            case ">":
                criteria.gt(convertToDouble(value));
                break;
            case ">=":
                criteria.gte(convertToDouble(value));
                break;
            case "<":
                criteria.lt(convertToDouble(value));
                break;
            case "<=":
                criteria.lte(convertToDouble(value));
                break;
            default:
                criteria.is(value);
        }
        return criteria;
    }

    private static Object convertToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        final Double result = getDouble(obj.toString());
        return result == null ? obj : result;
    }

    private static Double getDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            LOGGER.warn("Unable to convert value " + str + " to double.");
        }
        return null;
    }

    public BatchSearchAggregationBuilder withSearchQuery(String searchQuery) {
        List<Criteria> fieldCriteriaList = SEARCH_QUERY_FIELDS.stream().map(
                field -> Criteria.where(CONTENT_PREFIX + field).is(searchQuery)).
                collect(toList());

        Criteria[] fieldCriteriaArr = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
        Criteria orCriteria = new Criteria().orOperator(fieldCriteriaArr);
        aggregationOperations.add(Aggregation.match(orCriteria));
        return this;
    }

    public BatchSearchAggregationBuilder withBingoIds(List<Integer> bingoIds) {
        Criteria structureCriteria = Criteria.where(CONTENT_PREFIX + "structure.structureId").in(bingoIds);
        aggregationOperations.add(Aggregation.match(structureCriteria));
        return this;
    }

    public BatchSearchAggregationBuilder withAdvancedCriteria(List<BatchSearchCriteria> batchCriteriaList) {
        List<Criteria> fieldCriteriaList = batchCriteriaList.stream()
                .map(BatchSearchAggregationBuilder::convertCriteria).collect(toList());
        Criteria[] mongoCriteriaList = fieldCriteriaList.toArray(new Criteria[fieldCriteriaList.size()]);
        Criteria andCriteria = new Criteria().andOperator(mongoCriteriaList);
        aggregationOperations.add(Aggregation.match(andCriteria));
        return this;
    }

    public Aggregation build() {
        return Aggregation.newAggregation(aggregationOperations);
    }

}
