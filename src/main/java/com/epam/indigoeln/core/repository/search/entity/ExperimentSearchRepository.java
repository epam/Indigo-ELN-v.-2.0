package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.mongodb.BasicDBList;
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

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("status","name");
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

    public Optional<Set<String>> withBingoIds(StructureSearchType type, List<String> bingoIds) {
        return componentSearchRepository.withBingoIds(type, bingoIds)
                .map(Criteria.where("components")::in).map(this::find);
    }

    public Optional<Set<String>> withQuerySearch(String querySearch) {
        List<Criteria> searchCriteria = SEARCH_QUERY_FIELDS.stream().map(
                field -> Criteria.where(field).regex(".*" + querySearch + ".*","i")).
                collect(toList());
        componentSearchRepository.withQuerySearch(querySearch)
                .map(Criteria.where("components")::in).ifPresent(searchCriteria::add);
        return Optional.of(searchCriteria).map(ac ->
                        new Criteria().orOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]))
        ).map(this::find);
    }

    public Optional<Set<String>> withAdvancedCriteria(List<SearchCriterion> criteria) {
        List<Criteria> searchCriteria = criteria.stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        componentSearchRepository.withAdvancedCriteria(criteria)
                .map(Criteria.where("components")::in).ifPresent(searchCriteria::add);
        if (searchCriteria.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(searchCriteria).map(ac ->
                            new Criteria().andOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]))
            ).map(this::find);
        }
    }

    private Set<String> find(Criteria criteria) {
        return ((BasicDBList) template.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .map(o -> (String) o)
                .collect(Collectors.toSet());
    }

}

