package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchCriteria;

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

    private static final String CONTENT_PREFIX = "content.batches.";
    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("nbkBatch", "molFormula", "molWeight",
            "chemicalName", "externalNumber", "compoundState", "comments", "hazardComments", "casNumber");

    public static BatchSearchAggregationBuilder getInstance() {
        return new BatchSearchAggregationBuilder();
    }

    private List<AggregationOperation> aggregationOperations;

    private BatchSearchAggregationBuilder() {
        aggregationOperations = new ArrayList<>();
        aggregationOperations.add(Aggregation.match(Criteria.where("name").is("productBatchSummary"))); // filter by type
        aggregationOperations.add(Aggregation.project("content"));
        aggregationOperations.add(Aggregation.unwind("content.batches")); // unwind (flat array of batches)
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

    private static Criteria convertCriteria(BatchSearchCriteria dto) {
        Criteria criteria = Criteria.where(CONTENT_PREFIX + dto.getField());
        Object value = dto.getValue();
        switch (dto.getCondition()) {
            case "contains" :
                criteria.regex(".*" + value + ".*"); break;
            case "starts with" :
                criteria.regex(value + ".*"); break;
            case "ends with" :
                criteria.regex(".*" + value); break;
            case ">" :
                criteria.gt(value); break;
            case ">=" :
                criteria.gte(value); break;
            case "<" :
                criteria.lt(value); break;
            case "<=" :
                criteria.lte(value); break;
            default:
                criteria.is(value);
        }
        return criteria;
    }

}
