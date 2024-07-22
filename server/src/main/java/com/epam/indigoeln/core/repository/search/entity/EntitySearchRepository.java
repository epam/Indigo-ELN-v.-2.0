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

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.util.AuthoritiesUtil;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
