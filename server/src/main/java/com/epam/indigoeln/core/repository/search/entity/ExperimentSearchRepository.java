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

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.search.AggregationUtils;
import com.epam.indigoeln.core.repository.search.ResourceUtils;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.SearchCriterion;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
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
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Component
public class ExperimentSearchRepository implements InitializingBean {

    private static final List<String> SEARCH_QUERY_FIELDS = Arrays.asList("status", "name");
    private static final Collection<String> AVAILABLE_FIELDS = Arrays.asList("status", "author.$id", "kind");
    private static final String EXPERIMENT = "Experiment";


    @Value("classpath:mongo/search/experiments.js")
    private Resource scriptResource;

    private ExecutableMongoScript searchScript;

    @Autowired
    private ComponentSearchRepository componentSearchRepository;

    @Autowired
    private MongoTemplate template;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        searchScript = new ExecutableMongoScript(ResourceUtils.loadFunction(scriptResource));
    }

    Optional<List<EntitySearchResultDTO>> searchExperiments(EntitySearchRequest searchRequest,
                                                            List<String> bingoIds, User user) {
        return search(searchRequest, bingoIds).map(ids -> {
                    final Iterable<Experiment> experiments = experimentRepository.findAllById(ids);

                    Map<String, String> notebookNameMap = new HashMap<>();
                    final Set<DBRef> dbRefs = ids.stream().map(id -> new DBRef("experiment", id))
                            .collect(Collectors.toSet());
                    notebookRepository.findByExperimentsIds(dbRefs).forEach(n -> n.getExperiments()
                            .forEach(e -> notebookNameMap.put(e.getId(), n.getName())));

                    return StreamSupport.stream(experiments.spliterator(), false).filter(
                            p -> PermissionUtil.hasEditorAuthorityOrPermissions(user, p.getAccessList(),
                                    UserPermission.READ_ENTITY)
                    ).map(ExperimentDTO::new).map(e -> convert(notebookNameMap.get(e.getFullId()), e))
                            .collect(Collectors.toList());

                });
    }

    @SuppressWarnings("unchecked")
    private Optional<Set<String>> search(EntitySearchRequest request, List<String> bingoIds) {
        if (checkConditions(request)) {
            Optional<Criteria> advancedCriteria = getAdvancedCriteria(request);
            Optional<Criteria> queryCriteria = getQueryCriteria(request);
            Optional<Criteria> experimentIdsWithBingoIds = getExperimentIdsWithBingoIds(request, bingoIds);
            return AggregationUtils.andCriteria(advancedCriteria, queryCriteria, experimentIdsWithBingoIds)
                    .map(this::find);
        } else {
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

    private Optional<Criteria> getExperimentIdsWithBingoIds(EntitySearchRequest request, List<String> bingoIds) {
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

    private boolean checkConditions(EntitySearchRequest request) {
        boolean correct = request.getAdvancedSearch().stream()
                .allMatch(c -> AVAILABLE_FIELDS.contains(c.getField())
                        || ComponentSearchRepository.AVAILABLE_FIELDS.contains(c.getField()));

        if (correct) {
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
        return ((List<String>) template.scriptOps().execute(searchScript, criteria.getCriteriaObject()))
                .stream()
                .collect(Collectors.toSet());
    }

    private EntitySearchResultDTO convert(String notebookName, ExperimentDTO experiment) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind("Experiment");
        result.setName(notebookName + "-" + experiment.getFullName());
        result.setDetails(getDetails(experiment));
        result.setProjectId(SequenceIdUtil.extractFirstId(experiment));
        result.setNotebookId(experiment.getParentId());
        result.setExperimentId(experiment.getId());
        return result;
    }

    private EntitySearchResultDTO.Details getDetails(ExperimentDTO experiment) {
        final EntitySearchResultDTO.Details details = getDetails((BasicDTO) experiment);

        String title = BatchComponentUtil
                .getConceptDetails(experiment.getComponents()).map(cd -> cd.getContent().getString("title"))
                .orElseGet(
                        () -> BatchComponentUtil.getReactionDetails(experiment.getComponents())
                                .map(cd -> cd.getContent().getString("title"))
                                .orElse(null)
                );
        details.setTitle(title);
        return details;
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
