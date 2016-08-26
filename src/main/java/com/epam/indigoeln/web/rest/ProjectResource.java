package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

@Api
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
     * GET  /projects -> Returns all projects for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @ApiOperation(value = "Returns all projects for current user for tree representation according to his permissions.", produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjectsByPermissions() {
        LOGGER.debug("REST request to get all projects according to user permissions");
        User user = userService.getUserWithAuthorities();
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes(user);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/all -> Returns all projects without checking for User permissions
     */
    @ApiOperation(value = "Returns all projects for current user for tree representation.", produces = "application/json")
    @RequestMapping(value = "/all",method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjects() {
        LOGGER.debug("REST request to get all projects without checking for permissions");
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes();
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/permissions/user-removable -> Returns true if user can be removed from project without problems
     */
    @ApiOperation(value = "Returns true if user can be removed from project without problems.", produces = "application/json")
    @RequestMapping(value = "/permissions/user-removable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> isUserRemovable(
            @ApiParam("Project id") String projectId,
            @ApiParam("User id") String userId
        ) {
        LOGGER.debug("REST request to check if user can be deleted from project's access list without problems");
        boolean result = projectService.isUserRemovable(projectId, userId);
        return ResponseEntity.ok(ImmutableMap.of("isUserRemovable", result));
    }

    /**
     * GET /notebooks/sub-creations -> Returns all notebooks available for experiment creation
     */
    @ApiOperation(value = "Returns all projects available for notebook creation.", produces = "application/json")
    @RequestMapping(value = "/sub-creations",method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShortEntityDTO>> getProjectsForNotebookCreation() {
        LOGGER.debug("REST request to get all projects available for notebook creation");
        List<ShortEntityDTO> result = projectService.getProjectsForNotebookCreation(userService.getUserWithAuthorities());
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions
     */
    @ApiOperation(value = "Returns project by it's id according to permissions.", produces = "application/json")
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> getProject(
            @ApiParam("Project id") @PathVariable String id
        ) {
        LOGGER.debug("REST request to get project: {}", id);
        User user = userService.getUserWithAuthorities();
        ProjectDTO project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(project);
    }

    /**
     * POST  /projects -> Creates project with OWNER's permissions for current User
     */
    @ApiOperation(value = "Creates project with OWNER's permissions for current user.", produces = "application/json")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> createProject(
            @ApiParam("Project to create") @RequestBody ProjectDTO project
        ) throws URISyntaxException {
        LOGGER.debug("REST request to create project: {}", project);
        ProjectDTO createdProject = projectService.createProject(project);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, createdProject.getName());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + createdProject.getId()))
                .headers(headers).body(createdProject);
    }

    /**
     * PUT  /projects -> Updates project according to User permissions
     */
    @ApiOperation(value = "Updates project according to permissions.", produces = "application/json")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> updateProject(
            @ApiParam("Project to update") @RequestBody ProjectDTO project
        ) {
        LOGGER.debug("REST request to update project: {}", project);
        User user = userService.getUserWithAuthorities();
        ProjectDTO updatedProject = projectService.updateProject(project, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedProject.getName());
        return ResponseEntity.ok().headers(headers).body(updatedProject);
    }

    /**
     * DELETE  /projects/:id -> Removes project with specified id
     */
    @ApiOperation(value = "Removes project.", produces = "application/json")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteProject(
            @ApiParam("Project id") @PathVariable String id
        ) {
        LOGGER.debug("REST request to remove project: {}", id);
        projectService.deleteProject(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }
}