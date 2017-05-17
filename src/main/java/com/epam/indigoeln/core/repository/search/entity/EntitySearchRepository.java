package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.web.rest.dto.BasicDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import com.mongodb.DBRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class EntitySearchRepository {

    private static final String KIND_PROJECT = "Project";
    private static final String KIND_NOTEBOOK = "Notebook";
    private static final String KIND_EXPERIMENT = "Experiment";

    @Autowired
    private ProjectSearchRepository projectSearchRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotebookSearchRepository notebookSearchRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private ExperimentSearchRepository experimentSearchRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    public List<EntitySearchResultDTO> findEntities(User user, EntitySearchRequest searchRequest, List<String> bingoIds) {

        final Optional<List<EntitySearchResultDTO>> projectResult = findProjectsIds(searchRequest).map(ids -> {
            final Iterable<Project> projects = projectRepository.findAll(ids);
            return StreamSupport.stream(projects.spliterator(), false).filter(
                    p -> PermissionUtil.hasPermissions(user.getId(), p.getAccessList(), UserPermission.READ_ENTITY)
            ).map(ProjectDTO::new).map(this::convert).collect(Collectors.toList());
        });

        final Optional<List<EntitySearchResultDTO>> notebookResult = findNotebooksIds(searchRequest).map(ids -> {
            final Iterable<Notebook> notebooks = notebookRepository.findAll(ids);
            return StreamSupport.stream(notebooks.spliterator(), false).filter(
                    n -> PermissionUtil.hasPermissions(user.getId(), n.getAccessList(), UserPermission.READ_ENTITY)
            ).map(NotebookDTO::new).map(this::convert).collect(Collectors.toList());
        });

        final Optional<List<EntitySearchResultDTO>> experimentResult = findExperimentsIds(searchRequest, bingoIds).map(ids -> {
            final Iterable<Experiment> experiments = experimentRepository.findAll(ids);

            Map<String, String> notebookNameMap = new HashMap<>();
            final Set<DBRef> dbRefs = ids.stream().map(id -> new DBRef("experiment", id)).collect(Collectors.toSet());
            notebookRepository.findByExperimentsIds(dbRefs).forEach(n -> n.getExperiments().stream().forEach(e -> notebookNameMap.put(e.getId(), n.getName())));

            return StreamSupport.stream(experiments.spliterator(), false).filter(
                    p -> PermissionUtil.hasPermissions(user.getId(), p.getAccessList(), UserPermission.READ_ENTITY)
            ).map(ExperimentDTO::new).map(e -> convert(notebookNameMap.get(e.getFullId()), e)).collect(Collectors.toList());

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

    private Optional<Set<String>> findProjectsIds(EntitySearchRequest request) {
        if (request.getSearchQuery().isPresent()) {
            return projectSearchRepository.withQuerySearch(request.getSearchQuery().get());
        } else {
            return projectSearchRepository.withAdvancedCriteria(request.getAdvancedSearch());
        }
    }

    private EntitySearchResultDTO convert(ProjectDTO project) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_PROJECT);
        result.setName(project.getName());
        result.setDetails(getDetails(project));
        return result;
    }

    private Optional<Set<String>> findNotebooksIds(EntitySearchRequest request) {
        if (request.getSearchQuery().isPresent()) {
            return notebookSearchRepository.withQuerySearch(request.getSearchQuery().get());
        } else {
            return notebookSearchRepository.withAdvancedCriteria(request.getAdvancedSearch());
        }
    }

    private EntitySearchResultDTO convert(NotebookDTO notebook) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_NOTEBOOK);
        result.setName(notebook.getName());
        result.setDetails(getDetails(notebook));
        return result;
    }

    private Optional<Set<String>> findExperimentsIds(EntitySearchRequest request, List<String> bingoIds) {
        if (!bingoIds.isEmpty()) {
            final StructureSearchType type = request.getStructure().get().getType().getName();
            return experimentSearchRepository.withBingoIds(type, bingoIds);
        } else if (request.getSearchQuery().isPresent()) {
            return experimentSearchRepository.withQuerySearch(request.getSearchQuery().get());
        } else {
            return experimentSearchRepository.withAdvancedCriteria(request.getAdvancedSearch());
        }
    }

    private EntitySearchResultDTO convert(String notebookName, ExperimentDTO experiment) {
        EntitySearchResultDTO result = new EntitySearchResultDTO();
        result.setKind(KIND_EXPERIMENT);
        if (experiment.getExperimentVersion() > 1 || !experiment.isLastVersion()) {
            result.setName(notebookName + "-" + experiment.getName() + " v" + experiment.getExperimentVersion());
        }else {
            result.setName(notebookName + "-" + experiment.getName());
        }
        result.setDetails(getDetails(experiment));
        return result;
    }

    private EntitySearchResultDTO.Details getDetails(ExperimentDTO experiment) {
        final EntitySearchResultDTO.Details details = getDetails((BasicDTO) experiment);

        String title = BatchComponentUtil.getConceptDetails(experiment.getComponents()).map(cd -> cd.getContent().getString("title"))
                .orElseGet(
                        () -> BatchComponentUtil.getReactionDetails(experiment.getComponents()).map(cd -> cd.getContent().getString("title"))
                                .orElse(null)
                );
        details.setTitle(title);
        return details;
    }

    private EntitySearchResultDTO.Details getDetails(BasicDTO dto) {
        EntitySearchResultDTO.Details details = new EntitySearchResultDTO.Details();
        details.setCreationDate(dto.getCreationDate());
        details.setAuthor(dto.getAuthor().getFullName());
        return details;
    }

}
