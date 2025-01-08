/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
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
public class ProjectSearchRepository implements InitializingBean {

    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_KEYWORDS = "keywords";
    private static final String FIELD_REFERENCES = "references";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_KIND = "kind";
    private static final String PROJECT = "Project";

    private static final String FIELD_AUTHOR_ID = FIELD_AUTHOR + ".$id";

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays
            .asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_KEYWORDS, FIELD_REFERENCES);
    private static final List<String> AVAILABLE_FIELDS = Arrays
            .asList(FIELD_DESCRIPTION, FIELD_NAME, FIELD_KEYWORDS, FIELD_REFERENCES, FIELD_AUTHOR_ID, FIELD_KIND);

    @Value("classpath:mongo/search/projects.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Autowired
    private MongoTemplate template;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    Optional<List<EntitySearchResultDTO>> searchProjects(EntitySearchRequest searchRequest, User user) {
        return search(searchRequest).map(ids -> {
            final Iterable<Project> projects = projectRepository.findAllById(ids);
            return StreamSupport.stream(projects.spliterator(), false).filter(
                    p -> PermissionUtil.hasEditorAuthorityOrPermissions(user, p.getAccessList(),
                            UserPermission.READ_ENTITY)
            ).map(ProjectDTO::new).map(this::convert).collect(Collectors.toList());
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
                    .map(c -> c.contains(PROJECT))
                    .orElse(true);
        }
        return false;
    }

    private Set<String> find(Criteria criteria) {
        return ((List<String>) template.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .collect(Collectors.toSet());
    }

    private EntitySearchResultDTO convert(ProjectDTO project) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind("Project");
        result.setName(project.getName());
        result.setDetails(getDetails(project));
        result.setProjectId(project.getId());
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
