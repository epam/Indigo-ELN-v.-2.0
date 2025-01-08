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
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/";
    private static final String ENTITY_NAME = "Notebook";

    private static final String PARENT_PATH_ID = "projects/{projectId:[\\d]+}/notebooks";
    private static final String PATH_ID = PARENT_PATH_ID + "/{id:[\\d]+}";

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookResource.class);

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private UserService userService;


    /**
     * GET  /notebooks/permissions/user-removable -> Returns true if user can be removed
     * from notebook without problems.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @param userId     User id
     * @return Returns true if user can be removed from notebook without problems
     */
    @Operation(summary = "Returns true if user can be removed from notebook without problems.")
    @RequestMapping(value = "notebooks/permissions/user-removable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> isUserRemovable(
            @Parameter(description = "Project id") String projectId,
            @Parameter(description = "Notebook id") String notebookId,
            @Parameter(description = "User id") String userId
    ) {
        LOGGER.debug("REST request to check if user can be deleted from notebook's access list without problems");
        boolean result = notebookService.isUserRemovable(projectId, notebookId, userId);
        return ResponseEntity.ok(ImmutableMap.of("isUserRemovable", result));
    }

    /**
     * GET /notebooks/sub-creations -> Returns all notebooks available for experiment creation.
     *
     * @return List with notebooks
     */
    @Operation(summary = "Returns all notebooks available for experiment creation.")
    @RequestMapping(value = "notebooks/sub-creations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShortEntityDTO>> getNotebooksForExperimentCreation() {
        LOGGER.debug("REST request to get all notebooks available for experiment creation");
        List<ShortEntityDTO> result = notebookService
                .getNotebooksForExperimentCreation(userService.getUserWithAuthorities());
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id according to User permissions.
     *
     * @param projectId Project id
     * @param id        Notebook id
     * @return Returns notebook with specified id according to User permissions
     */
    @Operation(summary = "Returns notebook by it's id according to permissions.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> getNotebook(
            @Parameter(description = "Project id") @PathVariable String projectId,
            @Parameter(description = "Notebook id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get notebook: {}", id);
        User user = userService.getUserWithAuthorities();
        NotebookDTO notebook = notebookService.getNotebookById(projectId, id, user);
        return ResponseEntity.ok(notebook);
    }


    /**
     * POST  /notebooks?:projectId -> Creates notebook with OWNER's permissions for current User.
     * as child for specified Project
     *
     * @param projectId Project's identifier
     * @param notebook  Notebook
     * @return Created notebook
     * @throws URISyntaxException If URI is not correct
     */
    @Operation(summary = "Creates notebook with OWNER's permissions for current user.")
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> createNotebook(
            @Parameter(description = "Project id") @PathVariable String projectId,
            @Parameter(description = "Notebook to create") @RequestBody @Valid NotebookDTO notebook
    ) throws URISyntaxException {
        LOGGER.debug("REST request to create notebook: {} for project: {}", notebook, projectId);
        User user = userService.getUserWithAuthorities();
        NotebookDTO createdNotebook = notebookService.createNotebook(notebook, projectId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, createdNotebook.getName());
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + createdNotebook.getId()))
                .headers(headers).body(createdNotebook);
    }

    /**
     * PUT  /notebooks/:id -> Updates notebook according to User permissions.
     *
     * @param projectId Project's identifier
     * @param notebook  Notebook
     * @return Updated notebook
     */
    @Operation(summary = "Updates notebook according to permissions.")
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> updateNotebook(
            @Parameter(description = "Project id") @PathVariable String projectId,
            @Parameter(description = "Notebook to update") @RequestBody @Valid NotebookDTO notebook
    ) {
        LOGGER.debug("REST request to update notebook: {}", notebook);
        User user = userService.getUserWithAuthorities();
        NotebookDTO updatedNotebook = notebookService.updateNotebook(notebook, projectId, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedNotebook.getName());
        return ResponseEntity.ok().headers(headers).body(updatedNotebook);
    }

    /**
     * DELETE  /notebooks/:id -> Removes notebook with specified id.
     *
     * @param projectId Project's identifier
     * @param id        Identifier
     */
    @Operation(summary = "Removes notebook.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotebook(
            @Parameter(description = "Project id") @PathVariable String projectId,
            @Parameter(description = "Notebook id") @PathVariable String id) {
        LOGGER.debug("REST request to remove notebook: {}", id);
        notebookService.deleteNotebook(projectId, id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }

    /**
     * Checks if notebook name is new or not
     *
     * @param name Notebook name to check
     * @return Map with only one key where value is true or false
     */
    @Operation(summary = "Checks if notebook name is new or not")
    @RequestMapping(value = "notebooks/new", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> isNew(@Parameter(description = "Notebook name to check") @RequestParam String name) {
        boolean isNew = notebookService.isNew(name);
        return ResponseEntity.ok(Collections.singletonMap("isNew", isNew));
    }
}
