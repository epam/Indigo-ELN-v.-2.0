package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/";
    private static final String ENTITY_NAME = "Notebook";

    private static final String PARENT_PATH_ID = "projects/{projectId:[\\d]+}/notebooks";
    private static final String PATH_ID = PARENT_PATH_ID + "/{id:[\\d]+}";
    private static final String TREE_NODE_PATH_ID = PARENT_PATH_ID + "/notebook/{id:[\\d]+}";

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookResource.class);

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private UserService userService;

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
            value = PARENT_PATH_ID,
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
     * GET  /notebooks/notebook/:id -> Returns notebook with specified id for tree.
     *
     * @param id Notebook id
     * @return Returns notebook with specified id
     */
    @ApiOperation(value = "Returns notebook by it's id for tree.")
    @RequestMapping(value = TREE_NODE_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TreeNodeDTO> getNotebookAsTree(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get notebook: {}", id);
        TreeNodeDTO notebook = notebookService.getNotebookAsTreeNode(projectId, id);
        return ResponseEntity.ok(notebook);
    }

    /**
     * GET  /notebooks/all?:projectId -> Returns all notebooks of specified project.
     * without checking for User permissions.
     *
     * @param projectId Project's identifier
     * @return List with notebooks
     */
    @ApiOperation(value = "Returns all notebooks of specified project for current user for tree representation.")
    @RequestMapping(value = PARENT_PATH_ID + "/all", method = RequestMethod.GET,
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
     * GET  /notebooks/permissions/user-removable -> Returns true if user can be removed
     * from notebook without problems.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @param userId     User id
     * @return Returns true if user can be removed from notebook without problems
     */
    @ApiOperation(value = "Returns true if user can be removed from notebook without problems.")
    @RequestMapping(value = "notebooks/permissions/user-removable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> isUserRemovable(
            @ApiParam("Project id") String projectId,
            @ApiParam("Notebook id") String notebookId,
            @ApiParam("User id") String userId
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
    @ApiOperation(value = "Returns all notebooks available for experiment creation.")
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
    @ApiOperation(value = "Returns notebook by it's id according to permissions.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> getNotebook(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String id
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
    @ApiOperation(value = "Creates notebook with OWNER's permissions for current user.")
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> createNotebook(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook to create") @RequestBody @Valid NotebookDTO notebook
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
    @ApiOperation(value = "Updates notebook according to permissions.")
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> updateNotebook(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook to update") @RequestBody @Valid NotebookDTO notebook
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
    @ApiOperation(value = "Removes notebook.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotebook(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String id) {
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
    @ApiOperation(value = "Checks if notebook name is new or not")
    @RequestMapping(value = "notebooks/new", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> isNew(@ApiParam("Notebook name to check") @RequestParam String name) {
        boolean isNew = notebookService.isNew(name);
        return ResponseEntity.ok(Collections.singletonMap("isNew", isNew));
    }
}
