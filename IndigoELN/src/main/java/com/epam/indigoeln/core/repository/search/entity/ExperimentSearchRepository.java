package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
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

    @Autowired
    private ComponentSearchRepository componentSearchRepository;

    @Autowired
    private MongoTemplate template;

    @Value("classpath:mongo/search/experiments.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    public Optional<Set<String>> search(EntitySearchRequest request, List<String> bingoIds) {
        Optional<Criteria> advancedCriteria = getAdvancedCriteria(request);
        Optional<Criteria> queryCriteria = getQueryCriteria(request);
        Optional<Criteria> experimentIdsWithBingoIds = getExperimentIdsWithBingoIds(request, bingoIds);
        return AggregationUtils.andCriteria(this::find, advancedCriteria, queryCriteria, experimentIdsWithBingoIds);
    }

    private Optional<Criteria> getComponentWithBingoIds(EntitySearchRequest request, List<String> bingoIds) {
        Optional<Set<DBRef>> dbRefs = componentSearchRepository.searchWithBingoIds(request, bingoIds);
        return dbRefs.map(Criteria.where("components")::in);
    }

    private Optional<Criteria> getComponentWithQuery(EntitySearchRequest request) {
        Optional<Set<DBRef>> dbRefs = componentSearchRepository.searchWithQuery(request);
        return dbRefs.map(Criteria.where("components")::in);
    }

    private Optional<Criteria> getComponentWithAdvanced(EntitySearchRequest request) {
        Optional<Set<DBRef>> dbRefs = componentSearchRepository.searchWithAdvanced(request);
        return dbRefs.map(Criteria.where("components")::in);
    }

    private Optional<Criteria> getAdvancedCriteria(EntitySearchRequest request) {
        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());

        getComponentWithAdvanced(request).ifPresent(advancedSearch::add);
        if (!advancedSearch.isEmpty()) {
            return Optional.of(new Criteria().andOperator(advancedSearch.toArray(new Criteria[advancedSearch.size()])));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Criteria> getExperimentIdsWithBingoIds(EntitySearchRequest request, List<String> bingoIds){
        Optional<Criteria> componentWithBingoIds = getComponentWithBingoIds(request, bingoIds);
        return componentWithBingoIds.map(this::find).map(Criteria.where("_id")::in);
    }

    private Optional<Criteria> getQueryCriteria(EntitySearchRequest request) {
        List<String> fields = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(c -> c.getField())
                .collect(toList());

        List<Criteria> querySearch = new ArrayList<>();
        request.getSearchQuery().ifPresent(query -> SEARCH_QUERY_FIELDS.stream()
                .filter(field -> !fields.contains(field))
                .map(field -> Criteria.where(field).regex(".*" + query + ".*", "i"))
                .forEach(querySearch::add));

        getComponentWithQuery(request).ifPresent(querySearch::add);
        if (!querySearch.isEmpty()) {
            return Optional.of(new Criteria().orOperator(querySearch.toArray(new Criteria[querySearch.size()])));
        } else {
            return Optional.empty();
        }
    }

    private Set<String> find(Criteria criteria) {
        return ((BasicDBList) template.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .map(o -> (String) o)
                .collect(Collectors.toSet());
    }

}

