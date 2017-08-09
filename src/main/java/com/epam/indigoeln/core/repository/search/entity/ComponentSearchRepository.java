package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.BasicDBList;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class ComponentSearchRepository implements InitializingBean {

    public static final String CONDITION_CONTAINS = "contains";
    public static final String CONDITION_EQUALS = "=";
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("therapeuticArea", "projectCode", "batchYield", "purity", "name", "description", "compoundId",
            "references", "keywords", "chemicalName");
    private static final Collection<String> SEARCH_QUERY_EQ_FIELDS = Arrays.asList("batchYield", "purity");
    private static final Collection<String> SEARCH_QUERY_CON_FIELDS = Arrays.asList("therapeuticArea", "projectCode", "name", "description", "compoundId", "references", "keywords", "chemicalName");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("classpath:mongo/search/components.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    public Optional<Set<DBRef>> searchWithQuery(EntitySearchRequest request) {
        List<String> fields = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(c -> c.getField())
                .collect(toList());

        List<Criteria> querySearch = new ArrayList<>();
        request.getSearchQuery().ifPresent(query -> {
            SEARCH_QUERY_CON_FIELDS.stream()
                    .filter(field -> !fields.contains(field))
                    .map(f -> new SearchCriterion(f, f, CONDITION_CONTAINS, query))
                    .map(AggregationUtils::createCriterion)
                    .forEach(querySearch::add);

            SEARCH_QUERY_EQ_FIELDS.stream()
                    .filter(field -> !fields.contains(field))
                    .map(f -> new SearchCriterion(f, f, CONDITION_EQUALS, query))
                    .map(AggregationUtils::createCriterion)
                    .forEach(querySearch::add);
        });

        if (!querySearch.isEmpty()) {
            return Optional.of(new Criteria().orOperator(querySearch.toArray(new Criteria[querySearch.size()]))).map(this::find);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<DBRef>> searchWithAdvanced(EntitySearchRequest request) {
        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());

        if (!advancedSearch.isEmpty()) {
            return Optional.of(new Criteria().andOperator(advancedSearch.toArray(new Criteria[advancedSearch.size()]))).map(this::find);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<DBRef>> searchWithBingoIds(EntitySearchRequest request, List<String> bingoIds) {
        if (!bingoIds.isEmpty()) {
            StructureSearchType type = request.getStructure().get().getType().getName();
            if (type == StructureSearchType.PRODUCT) {
                return Optional.of(Criteria.where("batchStructureId").in(bingoIds)).map(this::find);
            } else if (type == StructureSearchType.REACTION) {
                return Optional.of(Criteria.where("reactionStructureId").in(bingoIds)).map(this::find);
            }
        }
        return Optional.empty();
    }

    private Set<DBRef> find(Criteria criteria) {
        return ((BasicDBList) mongoTemplate.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .map(o -> (ObjectId) o)
                .map(id -> new DBRef("component", id))
                .collect(Collectors.toSet()
                );
    }

}
