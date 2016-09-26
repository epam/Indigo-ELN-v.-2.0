package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.BasicDBList;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentSearchAggregationBuilder {

    public static final String CONDITION_CONTAINS = "contains";
    public static final String CONDITION_EQUALS = "=";
    public static final String CONDITION_IN = "in";
    protected List<Aggregation> aggregations;
    private MongoTemplate mongoTemplate;
    private ExecutableMongoScript searchScript;
    private Optional<Criteria> args;

    public ComponentSearchAggregationBuilder(ApplicationContext context, MongoTemplate mongoTemplate) {

        this.mongoTemplate = mongoTemplate;
        final String function = loadFunction(context, "classpath:mongo/components/search.js");
        searchScript = new ExecutableMongoScript(function);

        aggregations = new ArrayList<>();

    }

    public ComponentSearchAggregationBuilder withBingoIds(StructureSearchType type, List<Integer> bingoIds) {
        SearchCriterion searchCriterion = null;
        if (type == StructureSearchType.Product) {
            searchCriterion = new SearchCriterion("batchStructureId", "batchStructureId", CONDITION_IN, bingoIds);
        } else if (type == StructureSearchType.Reaction) {
            searchCriterion = new SearchCriterion("reactionStructureId", "reactionStructureId", CONDITION_IN, bingoIds);
        }
        args = Optional.ofNullable(searchCriterion).map(AggregationUtils::createCriterion);
        return this;
    }

    public ComponentSearchAggregationBuilder withQuerySearch(String querySearch) {
        Collection<SearchCriterion> searchCriteria = new ArrayList<>();
        Stream.of("therapeuticArea", "projectCode", "name", "description", "compoundId", "references", "keywords", "chemicalName").map(
                f -> new SearchCriterion(f, f, CONDITION_CONTAINS, querySearch)
        ).forEach(searchCriteria::add);
        Stream.of("batchYield", "purity").map(
                f -> new SearchCriterion(f, f, CONDITION_EQUALS, querySearch)
        ).forEach(searchCriteria::add);
        args = Optional.of(searchCriteria).map(ac ->
                        new Criteria().orOperator(AggregationUtils.createCriteria(searchCriteria).toArray(new Criteria[searchCriteria.size()]))
        );
        return this;
    }

    public ComponentSearchAggregationBuilder withAdvancedCriteria(List<SearchCriterion> advancedCriteria) {
        args = Optional.ofNullable(advancedCriteria.isEmpty() ? null : advancedCriteria).map(ac ->
                        new Criteria().andOperator(AggregationUtils.createCriteria(advancedCriteria).toArray(new Criteria[advancedCriteria.size()]))
        );
        return this;
    }

    public Optional<Set<DBRef>> build() {
        return args.map(
                a -> ((BasicDBList) mongoTemplate.scriptOps().execute(searchScript, a.getCriteriaObject()))
                        .stream()
                        .map(o -> (ObjectId) o)
                        .map(id -> new DBRef("component", id))
                        .collect(Collectors.toSet()
                        )
        );
    }

    protected String loadFunction(ResourceLoader resourceLoader, String path) {

        Resource functionResource = resourceLoader.getResource(path);

        if (!functionResource.exists()) {
            throw new IndigoRuntimeException(String.format("Resource %s not found!", path));
        }

        InputStream inputStream;
        try {
            inputStream = functionResource.getInputStream();
        } catch (IOException e) {
            throw new IndigoRuntimeException(String.format("Cannot read file %s!", path), e);
        }
        try (Scanner scanner = new Scanner(inputStream)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

}
