package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
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
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Component
public class NotebookSearchRepository implements InitializingBean {

    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_KIND = "kind";
    private static final String FIELD_AUTHOR_ID = FIELD_AUTHOR + ".$id";
    private static final String NOTEBOOK = "Notebook";

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList(FIELD_DESCRIPTION, FIELD_NAME);
    private static final List<String> AVAILABLE_FIELDS = Arrays
            .asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_AUTHOR_ID, FIELD_KIND);

    @Value("classpath:mongo/search/notebooks.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Autowired
    private MongoTemplate template;

    @Autowired
    private NotebookRepository notebookRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    Optional<List<EntitySearchResultDTO>> searchNotebooks(EntitySearchRequest searchRequest, User user) {
        return search(searchRequest).map(ids -> {
            final Iterable<Notebook> notebooks = notebookRepository.findAll(ids);
            return StreamSupport.stream(notebooks.spliterator(), false).filter(
                    n -> PermissionUtil.hasEditorAuthorityOrPermissions(user, n.getAccessList(),
                            UserPermission.READ_ENTITY)
            ).map(NotebookDTO::new).map(this::convert).collect(Collectors.toList());
        });
    }

    @SuppressWarnings("unchecked")
    private Optional<Set<String>> search(EntitySearchRequest request) {
        if (checkConditions(request)) {
            Optional<Criteria> advancedCriteria = getAdvancedCriteria(request);
            Optional<Criteria> queryCriteria = getQueryCriteria(request);
            return AggregationUtils.andCriteria(advancedCriteria, queryCriteria).map(this::find);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Criteria> getAdvancedCriteria(EntitySearchRequest request) {
        List<Criteria> advancedSearch = request.getAdvancedSearch().stream()
                .map(AggregationUtils::createCriterion)
                .collect(toList());
        return AggregationUtils.andCriteria(advancedSearch);
    }

    private Optional<Criteria> getQueryCriteria(EntitySearchRequest request) {
        List<String> fields = request.getAdvancedSearch().stream()
                .map(SearchCriterion::getField)
                .collect(toList());

        List<Criteria> querySearch = new ArrayList<>();
        request.getSearchQuery().ifPresent(query -> SEARCH_QUERY_FIELDS.stream()
                .filter(field -> !fields.contains(field))
                .map(field -> Criteria.where(field).regex(".*" + query + ".*", "i"))
                .forEach(querySearch::add));

        return AggregationUtils.orCriteria(querySearch);
    }

    private boolean checkConditions(EntitySearchRequest request) {
        boolean correct = request.getAdvancedSearch().stream().allMatch(c -> AVAILABLE_FIELDS.contains(c.getField()));
        if (correct) {
            return request.getAdvancedSearch().stream()
                    .filter(FIELD_KIND::equals)
                    .findAny()
                    .map(AggregationUtils::convertToCollection)
                    .map(c -> c.contains(NOTEBOOK))
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

    private EntitySearchResultDTO convert(NotebookDTO notebook) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind("Notebook");
        result.setName(notebook.getName());
        result.setDetails(getDetails(notebook));
        result.setProjectId(notebook.getParentId());
        result.setNotebookId(notebook.getId());
        return result;
    }

    private EntitySearchResultDTO.Details getDetails(BasicDTO dto) {
        EntitySearchResultDTO.Details details = new EntitySearchResultDTO.Details();
        details.setCreationDate(dto.getCreationDate());
        if (dto.getAuthor() != null) {
            details.setAuthor(dto.getAuthor().getFullName());

        }
        return details;
    }
}