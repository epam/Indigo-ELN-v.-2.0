package com.epam.indigoeln.core.repository.search.entity;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import com.epam.indigoeln.core.util.AuthoritiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class EntitySearchRepository {

    @Autowired
    private ProjectSearchRepository projectSearchRepository;

    @Autowired
    private NotebookSearchRepository notebookSearchRepository;

    @Autowired
    private ExperimentSearchRepository experimentSearchRepository;


    public List<EntitySearchResultDTO> findEntities(User user, EntitySearchRequest searchRequest,
                                                    List<String> bingoIds) {
        Optional<List<EntitySearchResultDTO>> projectResult = Optional.empty();
        Optional<List<EntitySearchResultDTO>> notebookResult = Optional.empty();
        Optional<List<EntitySearchResultDTO>> experimentResult = Optional.empty();

        if (bingoIds.isEmpty()) {
            if (AuthoritiesUtil.canReadProject(user)) {
                projectResult = projectSearchRepository.searchProjects(searchRequest, user);
            }
            if (AuthoritiesUtil.canReadNotebook(user)) {
                notebookResult = notebookSearchRepository.searchNotebooks(searchRequest, user);
            }
        }
        if (AuthoritiesUtil.canReadExperiment(user)) {
            experimentResult = experimentSearchRepository.searchExperiments(searchRequest, bingoIds, user);
        }
        return merge(projectResult, notebookResult, experimentResult);

    }

    private List<EntitySearchResultDTO> merge(Optional<List<EntitySearchResultDTO>> projectResult,
                                              Optional<List<EntitySearchResultDTO>> notebookResult,
                                              Optional<List<EntitySearchResultDTO>> experimentResult) {
        List<EntitySearchResultDTO> result = new ArrayList<>();
        projectResult.ifPresent(result::addAll);
        notebookResult.ifPresent(result::addAll);
        experimentResult.ifPresent(result::addAll);
        return result;
    }
}
