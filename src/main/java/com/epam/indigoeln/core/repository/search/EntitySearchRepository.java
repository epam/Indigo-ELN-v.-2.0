package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class EntitySearchRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchComponentsRepository.class);

    private static final String KIND_PROJECT = "Project";
    private static final String KIND_NOTEBOOK = "Notebook";
    private static final String KIND_EXPERIMENT = "Experiment";
    private static Function<DBObject, EntitySearchResultDTO> CONVERT_PROJECT = dbObject -> {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_PROJECT);
        result.setName((String) dbObject.get("name"));
        result.setCreationDate((Date) dbObject.get("creationDate"));
        return result;
    };
    private static Function<DBObject, EntitySearchResultDTO> CONVERT_NOTEBOOK = dbObject -> {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_NOTEBOOK);
        result.setName((String) dbObject.get("name"));
        result.setCreationDate((Date) dbObject.get("creationDate"));
        return result;
    };
    private static Function<DBObject, EntitySearchResultDTO> CONVERT_EXPERIMENT = dbObject -> {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_EXPERIMENT);
        result.setName((String) dbObject.get("name"));
        result.setCreationDate((Date) dbObject.get("creationDate"));
        return result;
    };
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<EntitySearchResultDTO> findEntities(EntitySearchRequest searchRequest) {

        Optional<Aggregation> aggregation = buildProjectAggregation(searchRequest);
        final Optional<List<EntitySearchResultDTO>> projectResult = aggregation.map(agg -> {
            LOGGER.debug("Perform project search query: " + agg.toString());
            List<DBObject> mappedResults = mongoTemplate.aggregate(agg, Project.class, DBObject.class).getMappedResults();

            return mappedResults.stream().map(CONVERT_PROJECT).collect(Collectors.toList());
        });

        aggregation = buildNotebookAggregation(searchRequest);
        final Optional<List<EntitySearchResultDTO>> notebookResult = aggregation.map(agg -> {
            LOGGER.debug("Perform notebook search query: " + agg.toString());
            List<DBObject> mappedResults = mongoTemplate.aggregate(agg, Notebook.class, DBObject.class).getMappedResults();

            return mappedResults.stream().map(CONVERT_NOTEBOOK).collect(Collectors.toList());
        });

        aggregation = buildExperimentAggregation(searchRequest);
        final Optional<List<EntitySearchResultDTO>> experimentResult = aggregation.map(agg -> {
            LOGGER.debug("Perform experiment search query: " + agg.toString());
            List<DBObject> mappedResults = mongoTemplate.aggregate(agg, Experiment.class, DBObject.class).getMappedResults();

            return mappedResults.stream().map(CONVERT_EXPERIMENT).collect(Collectors.toList());
        });
        return merge(projectResult, notebookResult, experimentResult);

    }

    private List<EntitySearchResultDTO> merge(Optional<List<EntitySearchResultDTO>> projectResult, Optional<List<EntitySearchResultDTO>> notebookResult,
                                              Optional<List<EntitySearchResultDTO>> experimentResult) {
        List<EntitySearchResultDTO> result = new ArrayList<>();
        projectResult.ifPresent(result::addAll);
        notebookResult.ifPresent(result::addAll);
        experimentResult.ifPresent(result::addAll);
        return result;
    }

    private Optional<Aggregation> buildProjectAggregation(EntitySearchRequest request) {
        final Boolean sameKind = request.getSearchKind().map(KIND_PROJECT::equalsIgnoreCase).orElse(true);
        if (!sameKind) {
            return Optional.empty();
        }
        ProjectSearchAggregationBuilder builder = ProjectSearchAggregationBuilder.getInstance();
        request.getSearchQuery().ifPresent(builder::withSearchQuery);

        if (!request.getAdvancedSearch().isEmpty()) {
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        return builder.build();
    }

    private Optional<Aggregation> buildNotebookAggregation(EntitySearchRequest request) {
        final Boolean sameKind = request.getSearchKind().map(KIND_NOTEBOOK::equalsIgnoreCase).orElse(true);
        if (!sameKind) {
            return Optional.empty();
        }
        NotebookSearchAggregationBuilder builder = NotebookSearchAggregationBuilder.getInstance();
        request.getSearchQuery().ifPresent(builder::withSearchQuery);

        if (!request.getAdvancedSearch().isEmpty()) {
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        return builder.build();
    }

    private Optional<Aggregation> buildExperimentAggregation(EntitySearchRequest request) {
        final Boolean sameKind = request.getSearchKind().map(KIND_EXPERIMENT::equalsIgnoreCase).orElse(true);
        if (!sameKind) {
            return Optional.empty();
        }
        ExperimentSearchAggregationBuilder builder = ExperimentSearchAggregationBuilder.getInstance();
        request.getSearchQuery().ifPresent(builder::withSearchQuery);

        if (!request.getAdvancedSearch().isEmpty()) {
            builder.withAdvancedCriteria(request.getAdvancedSearch());
        }

        return builder.build();
    }

}
