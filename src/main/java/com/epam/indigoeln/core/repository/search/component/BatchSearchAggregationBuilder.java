package com.epam.indigoeln.core.repository.search.component;

import com.epam.indigoeln.core.repository.search.AbstractSearchAggregationBuilder;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.List;

/**
 * Builds Aggregation For Search
 *
 * Mongo DB Example :
 *
 * db.component.aggregate(
 *      {$match: {'name' :'productBatchSummary'}},
 *      {$project: {$content}},
 *      {$unwind : "$content.batches"},
 *      {$match: {"content.batches.formula" : "C6 H6"}}
 * )
 *
 */
public final class BatchSearchAggregationBuilder extends AbstractSearchAggregationBuilder {

    private static final String CONTENT_PREFIX = "content.batches.";
    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("nbkBatch", "formula", "molWeight",
            "chemicalName", "externalNumber", "compoundState", "comments", "hazardComments", "casNumber");

    private BatchSearchAggregationBuilder() {
        aggregationOperations.add(Aggregation.match(Criteria.where("name").is("productBatchSummary"))); // filter by type
        aggregationOperations.add(Aggregation.project("content"));
        aggregationOperations.add(Aggregation.unwind("content.batches")); // unwind (flat array of batches)
        setContextPrefix(CONTENT_PREFIX);
        setSearchQueryFields(SEARCH_QUERY_FIELDS);
    }

    public static BatchSearchAggregationBuilder getInstance() {
        return new BatchSearchAggregationBuilder();
    }

    public BatchSearchAggregationBuilder withBingoIds(List<String> bingoIds) {
        Criteria structureCriteria = Criteria.where(CONTENT_PREFIX + "structure.structureId").in(bingoIds);
        aggregationOperations.add(Aggregation.match(structureCriteria));
        return this;
    }

    public BatchSearchAggregationBuilder withLimit(Integer batchesLimit){
        aggregationOperations.add(Aggregation.limit(batchesLimit));
        return this;
    }

    public Aggregation build() {
        return Aggregation.newAggregation(aggregationOperations);
    }

}
