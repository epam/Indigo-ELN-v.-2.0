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
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping(TreeResource.URL_MAPPING)
public class TreeResource {

    static final String URL_MAPPING = "/api/tree";
    private static final String PROJECTS_PATH = "/projects";
    private static final String NOTEBOOKS_PATH = PROJECTS_PATH + "/{projectId:[\\d]+}/notebooks";
    private static final String EXPERIMENTS_PATH = NOTEBOOKS_PATH + "/{notebookId:[\\d]+}/experiments";
    private static final String PROJECT_PATH_ID = "projects/{projectId:[\\d]+}";
    private static final String NOTEBOOK_PATH_ID = PROJECT_PATH_ID + "/notebooks/{notebookId:[\\d]+}";
    private static final String EXPERIMENT_PATH_ID = NOTEBOOK_PATH_ID + "/experiments/{experimentId:[\\d]+}";
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeResource.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions.
     *
     * @param projectId Identifier
     * @return Project
     */
    @ApiOperation(value = "Returns project by it's id as tree node.")
    @RequestMapping(value = PROJECT_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TreeNodeDTO> getProjectForTree(
            @ApiParam("Project id") @PathVariable String projectId
    ) {
        LOGGER.debug("REST request to get project tree node: {}", projectId);
        TreeNodeDTO project = projectService.getProjectAsTreeNode(projectId);
        return ResponseEntity.ok(project);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id for tree.
     *
     * @param notebookId Notebook id
     * @return Returns notebook with specified id
     */
    @ApiOperation(value = "Returns notebook by it's id for tree.")
    @RequestMapping(value = NOTEBOOK_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TreeNodeDTO> getNotebookAsTree(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
    ) {
        LOGGER.debug("REST request to get notebook: {}", notebookId);
        TreeNodeDTO notebook = notebookService.getNotebookAsTreeNode(projectId, notebookId);
        return ResponseEntity.ok(notebook);
    }


    @ApiOperation(value = "Returns experiment with specified id for tree.")
    @RequestMapping(value = EXPERIMENT_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentTreeNodeDTO> getExperimentAsTreeNode(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String experimentId
    ) {
        ExperimentTreeNodeDTO experimentTreeNodeDTO = experimentService
                .getExperimentAsTreeNode(projectId, notebookId, experimentId);
        return ResponseEntity.ok(experimentTreeNodeDTO);
    }

    /**
     * GET  /projects -> Returns all projects for <b>current user</b>
     * for tree representation according to his User permissions.
     *
     * @return Returns all projects for current user for tree representation according to his permissions
     */
    @ApiOperation(value = "Returns all projects for current user for tree representation according to his permissions.")
    @RequestMapping(value = PROJECTS_PATH, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjectsByPermissions() {
        LOGGER.debug("REST request to get all projects according to user permissions");
        User user = userService.getUserWithAuthorities();
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes(user);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/all -> Returns all projects without checking for User permissions.
     *
     * @return Returns all projects for current user for tree representation
     */
    @ApiOperation(value = "Returns all projects for current user for tree representation.")
    @RequestMapping(value = PROJECTS_PATH + "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjects() {
        LOGGER.debug("REST request to get all projects without checking for permissions");
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes();
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks?:projectId -> Returns all notebooks of specified project for <b>current user</b>
     * for tree representation according to his User permissions.
     *
     * @param projectId Project id
     * @return List with notebooks
     */
    @ApiOperation(value = "Returns all notebooks of specified project "
            + "for current user for tree representation according to his permissions.")
    @RequestMapping(
            value = NOTEBOOKS_PATH,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PostFilter("hasAnyAuthority(T(com.epam.indigoeln.core.util.AuthoritiesUtil).NOTEBOOK_READERS)")
    @ResponseStatus(value = HttpStatus.OK)
    public List<TreeNodeDTO> getAllNotebooksByPermissions(
            @ApiParam("Project id") @PathVariable String projectId) {
        LOGGER.debug("REST request to get all notebooks of project: {} according to user permissions", projectId);
        User user = userService.getUserWithAuthorities();
        return notebookService.getAllNotebookTreeNodes(projectId, user);
    }

    /**
     * GET  /notebooks/all?:projectId -> Returns all notebooks of specified project.
     * without checking for User permissions.
     *
     * @param projectId Project's identifier
     * @return List with notebooks
     */
    @ApiOperation(value = "Returns all notebooks of specified project for current user for tree representation.")
    @RequestMapping(value = NOTEBOOKS_PATH + "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllNotebooks(
            @ApiParam("Project id") @PathVariable String projectId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all notebooks of project: {} "
                    + "without checking for permissions", projectId);
        }
        List<TreeNodeDTO> result = notebookService.getAllNotebookTreeNodes(projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @return Returns all experiments, or experiments for specified notebook, which author is current user
     */
    @ApiOperation(value = "Returns all experiments, or experiments for specified notebook, "
            + "which author is current user.")
    @RequestMapping(value = EXPERIMENTS_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostFilter("hasAnyAuthority(T(com.epam.indigoeln.core.util.AuthoritiesUtil).EXPERIMENT_READERS)")
    @ResponseStatus(HttpStatus.OK)
    public List<ExperimentTreeNodeDTO> getAllExperimentsByPermissions(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
    ) {
        User user = userService.getUserWithAuthorities();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all experiments of notebook: {} "
                    + "according to user permissions", notebookId);
        }
        return experimentService.getAllExperimentTreeNodes(projectId, notebookId, user);
    }

    /**
     * GET  /notebooks/:notebookId/experiments/all -> Returns all experiments of specified notebook
     * without checking for User permissions.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @return Returns all experiments of specified notebook
     */
    @ApiOperation(value = "Returns all experiments of specified notebook for current user for tree representation")
    @RequestMapping(value = EXPERIMENTS_PATH + "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExperimentTreeNodeDTO>> getAllExperiments(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all experiments of notebook: {} "
                    + "without checking for permissions", notebookId);
        }
        List<ExperimentTreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(projectId, notebookId);
        return ResponseEntity.ok(result);
    }
}