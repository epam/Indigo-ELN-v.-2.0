package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class ComponentSearchRepository implements InitializingBean {

    public static final String CONDITION_CONTAINS = "contains";
    public static final String CONDITION_EQUALS = "=";
    public static final String CONDITION_IN = "in";
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("therapeuticArea", "projectCode", "batchYield", "purity", "name", "description", "compoundId",
            "references", "keywords", "chemicalName");
    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("classpath:mongo/search/components.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    public Optional<Set<DBRef>> withBingoIds(StructureSearchType type, List<String> bingoIds) {
        SearchCriterion searchCriterion = null;
        if (type == StructureSearchType.Product) {
            searchCriterion = new SearchCriterion("batchStructureId", "batchStructureId", CONDITION_IN, bingoIds);
        } else if (type == StructureSearchType.Reaction) {
            searchCriterion = new SearchCriterion("reactionStructureId", "reactionStructureId", CONDITION_IN, bingoIds);
        }
        return Optional.ofNullable(searchCriterion).map(AggregationUtils::createCriterion).map(this::find);
    }

    public Optional<Set<DBRef>> withQuerySearch(String querySearch) {
        Collection<SearchCriterion> searchCriteria = new ArrayList<>();
        Stream.of("therapeuticArea", "projectCode", "name", "description", "compoundId", "references", "keywords", "chemicalName").map(
                f -> new SearchCriterion(f, f, CONDITION_CONTAINS, querySearch)
        ).forEach(searchCriteria::add);
        Stream.of("batchYield", "purity").map(
                f -> new SearchCriterion(f, f, CONDITION_EQUALS, querySearch)
        ).forEach(searchCriteria::add);
        return Optional.of(searchCriteria).map(ac ->
                        new Criteria().orOperator(AggregationUtils.createCriteria(searchCriteria).toArray(new Criteria[searchCriteria.size()]))
        ).map(this::find);
    }

    public Optional<Set<DBRef>> withAdvancedCriteria(List<SearchCriterion> criteria) {
        List<Criteria> searchCriteria = criteria.stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        if (searchCriteria.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(searchCriteria).map(ac ->
                            new Criteria().andOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]))
            ).map(this::find);
        }
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
