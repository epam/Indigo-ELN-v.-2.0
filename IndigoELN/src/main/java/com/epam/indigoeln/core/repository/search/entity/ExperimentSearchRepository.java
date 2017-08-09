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
public class ExperimentSearchRepository implements InitializingBean {

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("status", "name");
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("status", "author._id", "kind");
    private static final String EXPERIMENT = "Experiment";

    private final ComponentSearchRepository componentSearchRepository;

    private final MongoTemplate template;

    @Value("classpath:mongo/search/experiments.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Autowired
    public ExperimentSearchRepository(ComponentSearchRepository componentSearchRepository, MongoTemplate template) {
        this.componentSearchRepository = componentSearchRepository;
        this.template = template;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    @SuppressWarnings("unchecked")
    public Optional<Set<String>> search(EntitySearchRequest request, List<String> bingoIds) {
        if (checkConditions(request)){
            Optional<Criteria> advancedCriteria = getAdvancedCriteria(request);
            Optional<Criteria> queryCriteria = getQueryCriteria(request);
            Optional<Criteria> experimentIdsWithBingoIds = getExperimentIdsWithBingoIds(request, bingoIds);
            return AggregationUtils.andCriteria(advancedCriteria, queryCriteria, experimentIdsWithBingoIds).map(this::find);
        }else {
            return Optional.empty();
        }
    }

    private Optional<Criteria> getComponentWithBingoIds(EntitySearchRequest request, List<String> bingoIds) {
        Optional<Set<Object>> dbRefs = componentSearchRepository.searchWithBingoIds(request, bingoIds);
        return dbRefs.map(s -> Criteria.where("_id").in(s));
    }

    private Optional<Criteria> getComponentWithQuery(EntitySearchRequest request) {
        Optional<Set<Object>> dbRefs = componentSearchRepository.searchWithQuery(request);
        return dbRefs.map(s -> Criteria.where("_id").in(s));
    }

    private Optional<Criteria> getComponentWithAdvanced(EntitySearchRequest request) {
        Optional<Set<Object>> dbRefs = componentSearchRepository.searchWithAdvanced(request);
        return dbRefs.map(s -> Criteria.where("_id").in(s));
    }

    private Optional<Criteria> getAdvancedCriteria(EntitySearchRequest request) {
        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());

        getComponentWithAdvanced(request).ifPresent(advancedSearch::add);
        return AggregationUtils.andCriteria(advancedSearch);
    }

    private Optional<Criteria> getExperimentIdsWithBingoIds(EntitySearchRequest request, List<String> bingoIds){
        Optional<Criteria> componentWithBingoIds = getComponentWithBingoIds(request, bingoIds);
        return componentWithBingoIds.map(this::find).map(ids -> Criteria.where("_id").in(ids));
    }

    private Optional<Criteria> getQueryCriteria(EntitySearchRequest request) {
        List<String> fields = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(SearchCriterion::getField)
                .collect(toList());

        List<Criteria> querySearch = new ArrayList<>();
        request.getSearchQuery().ifPresent(query -> SEARCH_QUERY_FIELDS.stream()
                .filter(field -> !fields.contains(field))
                .map(field -> Criteria.where(field).regex(".*" + query + ".*", "i"))
                .forEach(querySearch::add));

        getComponentWithQuery(request).ifPresent(querySearch::add);
        return AggregationUtils.orCriteria(querySearch);
    }

    private boolean checkConditions(EntitySearchRequest request){
        boolean correct = request.getAdvancedSearch().stream()
                .allMatch(c -> AVAILABLE_FIELDS.contains(c.getField()) || ComponentSearchRepository.AVAILABLE_FIELDS.contains(c.getField()));

        if (correct){
            return request.getAdvancedSearch().stream()
                    .filter("kind"::equals)
                    .findAny()
                    .map(AggregationUtils::convertToCollection)
                    .map(c -> c.contains(EXPERIMENT))
                    .orElse(true);
        }
        return false;
    }

    private Set<String> find(Criteria criteria) {
        return ((BasicDBList) template.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .map(o -> (String) o)
                .collect(Collectors.toSet());
    }

}

