package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.BasicDBList;
import com.mongodb.DBRef;
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

    private static final String CONDITION_CONTAINS = "contains";
    private static final String CONDITION_EQUALS = "=";
    private static final String FIELD_PURITY = "purity";
    private static final String FIELD_PURITY_VALUE = "value";
    private static final String FIELD_PURITY_OPERATOR = "operator.name";

    public static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("therapeuticArea", "projectCode", "batchYield", FIELD_PURITY, "name", "description", "compoundId",
            "references", "keywords", "chemicalName");
    private static final Collection<String> SEARCH_QUERY_EQ_FIELDS = Arrays.asList("batchYield", FIELD_PURITY);
    private static final Collection<String> SEARCH_QUERY_CON_FIELDS = Arrays.asList("therapeuticArea", "projectCode", "name", "description", "compoundId", "references", "keywords", "chemicalName");

    private final MongoTemplate mongoTemplate;

    @Value("classpath:mongo/search/components.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Autowired
    public ComponentSearchRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    public Optional<Set<Object>> searchWithQuery(EntitySearchRequest request) {
        List<String> fields = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(SearchCriterion::getField)
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

        return AggregationUtils.orCriteria(querySearch).map(this::find);
    }

    public Optional<Set<Object>> searchWithAdvanced(EntitySearchRequest request) {
        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .filter(c -> !FIELD_PURITY.equals(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());

        Optional<SearchCriterion> purityCriteria = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .filter(c -> FIELD_PURITY.equals(c.getField()))
                .findAny();

        purityCriteria.ifPresent(c -> {
            Criteria criteria = Criteria.where(FIELD_PURITY_OPERATOR).is(c.getCondition())
                    .and(FIELD_PURITY_VALUE).is(AggregationUtils.convertToDouble(c.getValue()));
            advancedSearch.add(Criteria.where(FIELD_PURITY).elemMatch(criteria));
        });
        return AggregationUtils.andCriteria(advancedSearch).map(this::find);
    }

    public Optional<Set<Object>> searchWithBingoIds(EntitySearchRequest request, List<String> bingoIds) {
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

    private Set<Object> find(Criteria criteria) {
        return ((BasicDBList) mongoTemplate.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .map(o -> (DBRef) o)
                .map(DBRef::getId)
                .collect(Collectors.toSet()
                );
    }

}
