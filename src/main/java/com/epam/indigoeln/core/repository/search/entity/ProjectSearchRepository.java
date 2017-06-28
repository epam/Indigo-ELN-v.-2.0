package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
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
public class ProjectSearchRepository implements InitializingBean {

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_KEYWORDS = "keywords";
    public static final String FIELD_REFERENCES = "references";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_KIND = "kind";

    public static final String FIELD_AUTHOR_ID = FIELD_AUTHOR + "._id";

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_KEYWORDS, FIELD_REFERENCES);
    private static final List<String> AVAILABLE_FIELDS = Arrays.asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_KEYWORDS, FIELD_REFERENCES, FIELD_AUTHOR_ID, FIELD_KIND);

    @Autowired
    private MongoTemplate template;

    @Value("classpath:mongo/search/projects.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    public Optional<Set<String>> search(EntitySearchRequest request) {
        Optional<Criteria> advancedCriteria = getAdvancedCriteria(request);
        Optional<Criteria> queryCriteria = getQueryCriteria(request);
        return AggregationUtils.andCriteria(this::find, advancedCriteria, queryCriteria);
    }

    private Optional<Criteria> getAdvancedCriteria(EntitySearchRequest request) {
        if (!request.getAdvancedSearch().stream().allMatch(c -> AVAILABLE_FIELDS.contains(c.getField()))){
            return Optional.empty();
        }

        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .map(AggregationUtils::createCriterion)
                .collect(toList());

        if (!advancedSearch.isEmpty()) {
            return Optional.of(new Criteria().andOperator(advancedSearch.toArray(new Criteria[advancedSearch.size()])));
        }else {
            return Optional.empty();
        }
    }

    private Optional<Criteria> getQueryCriteria(EntitySearchRequest request) {
        if (!request.getAdvancedSearch().stream().allMatch(c -> AVAILABLE_FIELDS.contains(c.getField()))){
            return Optional.empty();
        }

        List<String> fields = request.getAdvancedSearch().stream()
                .filter(c -> AVAILABLE_FIELDS.contains(c.getField()))
                .map(c -> c.getField())
                .collect(toList());

        List<Criteria> querySearch = new ArrayList<>();
        request.getSearchQuery().ifPresent(query -> SEARCH_QUERY_FIELDS.stream()
                .filter(field -> !fields.contains(field))
                .map(field -> Criteria.where(field).regex(".*" + query + ".*", "i"))
                .forEach(querySearch::add));

        if (!querySearch.isEmpty()) {
            return Optional.of(new Criteria().orOperator(querySearch.toArray(new Criteria[querySearch.size()])));
        }else {
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
