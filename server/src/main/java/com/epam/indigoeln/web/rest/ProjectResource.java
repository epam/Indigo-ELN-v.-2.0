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
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ProjectResource.URL_MAPPING)
public class ProjectResource {

    static final String URL_MAPPING = "/api/projects";
    private static final String PATH_ID = "/{id:[\\d]+}";
    private static final String ENTITY_NAME = "Project";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectResource.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;


    /**
     * GET  /projects/permissions/user-removable -> Returns true if user can be removed from project without problems.
     *
     * @param projectId Project's identifier
     * @param userId    User's identifier
     * @return Returns true if user can be removed from project without problems
     */
    @Operation(summary = "Returns true if user can be removed from project without problems.")
    @RequestMapping(value = "/permissions/user-removable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> isUserRemovable(
            @Parameter(description = "Project id") String projectId,
            @Parameter(description = "User id") String userId
    ) {
        LOGGER.debug("REST request to check if user can be deleted from project's access list without problems");
        boolean result = projectService.isUserRemovable(projectId, userId);
        return ResponseEntity.ok(ImmutableMap.of("isUserRemovable", result));
    }

    /**
     * GET /notebooks/sub-creations -> Returns all notebooks available for experiment creation.
     *
     * @return List wit notebooks
     */
    @Operation(summary = "Returns all projects available for notebook creation.")
    @RequestMapping(value = "/sub-creations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShortEntityDTO>> getProjectsForNotebookCreation() {
        LOGGER.debug("REST request to get all projects available for notebook creation");
        List<ShortEntityDTO> result = projectService
                .getProjectsForNotebookCreation(userService.getUserWithAuthorities());
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions.
     *
     * @param id Identifier
     * @return Project
     */
    @Operation(summary = "Returns project by it's id according to permissions.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> getProject(
            @Parameter(description = "Project id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get project: {}", id);
        User user = userService.getUserWithAuthorities();
        ProjectDTO project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(project);
    }

    /**
     * POST  /projects -> Creates project with OWNER's permissions for current User.
     *
     * @param project Project
     * @return Created project
     * @throws URISyntaxException If URI is not correct
     */
    @Operation(summary = "Creates project with OWNER's permissions for current user.")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> createProject(
            @Parameter(description = "Project to create") @RequestBody ProjectDTO project
    ) throws URISyntaxException {
        LOGGER.debug("REST request to create project: {}", project);
        User user = userService.getUserWithAuthorities();
        ProjectDTO createdProject = projectService.createProject(project, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, createdProject.getName());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + createdProject.getId()))
                .headers(headers).body(createdProject);
    }

    /**
     * PUT  /projects -> Updates project according to User permissions.
     *
     * @param project Project to update
     * @return Updated project
     */
    @Operation(summary = "Updates project according to permissions.")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> updateProject(
            @Parameter(description = "Project to update") @RequestBody ProjectDTO project
    ) {
        LOGGER.debug("REST request to update project: {}", project);
        User user = userService.getUserWithAuthorities();
        ProjectDTO updatedProject = projectService.updateProject(project, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedProject.getName());
        return ResponseEntity.ok().headers(headers).body(updatedProject);
    }

    /**
     * DELETE  /projects/:id -> Removes project with specified id.
     *
     * @param id Identifier
     */
    @Operation(summary = "Removes project.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to remove project: {}", id);
        projectService.deleteProject(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }
}
