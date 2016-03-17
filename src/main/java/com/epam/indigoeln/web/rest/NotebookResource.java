package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ShortEntityDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/";
    private static final String ENTITY_NAME = "Notebook";

    private static final String PARENT_PATH_ID = "projects/{projectId:[\\d]+}/notebooks";
    private static final String PATH_ID = PARENT_PATH_ID + "/{id:[\\d]+}";

    private final Logger log = LoggerFactory.getLogger(NotebookResource.class);

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private UserService userService;

    /**
     * GET  /notebooks?:projectId -> Returns all notebooks of specified project for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(
            value = PARENT_PATH_ID,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllNotebooksByPermissions(@PathVariable String projectId) {
        log.debug("REST request to get all notebooks of project: {} according to user permissions", projectId);
        User user = userService.getUserWithAuthorities();
        List<TreeNodeDTO> result = notebookService.getAllNotebookTreeNodes(projectId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/all?:projectId -> Returns all notebooks of specified project
     * without checking for User permissions
     */
    @RequestMapping(value = PARENT_PATH_ID + "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllNotebooks(@PathVariable String projectId) {
        log.debug("REST request to get all notebooks of project: {} " +
                "without checking for permissions", projectId);
        List<TreeNodeDTO> result = notebookService.getAllNotebookTreeNodes(projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/permissions/user-removable -> Returns true if user can be removed from notebook without problems
     */
    @RequestMapping(value = "/permissions/user-removable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isUserRemovable(String notebookId, String userId) {
        log.debug("REST request to check if user can be deleted from notebook's access list without problems");
        boolean result = notebookService.isUserRemovable(notebookId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /notebooks/sub-creations -> Returns all notebooks available for experiment creation
     */
    @RequestMapping(value = "notebooks/sub-creations",method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShortEntityDTO>> getNotebooksForExperimentCreation() {
        log.debug("REST request to get all notebooks available for experiment creation");
        List<ShortEntityDTO> result = notebookService.getNotebooksForExperimentCreation(userService.getUserWithAuthorities());
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id according to User permissions
     */
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> getNotebook(@PathVariable String projectId, @PathVariable String id) {
        log.debug("REST request to get notebook: {}", id);
        User user = userService.getUserWithAuthorities();
        NotebookDTO notebook = notebookService.getNotebookById(projectId, id, user);
        return ResponseEntity.ok(notebook);
    }



    /**
     * POST  /notebooks?:projectId -> Creates notebook with OWNER's permissions for current User
     * as child for specified Project
     */
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> createNotebook(
            @PathVariable String projectId,
            @RequestBody @Valid NotebookDTO notebook) throws URISyntaxException {
        log.debug("REST request to create notebook: {} for project: {}", notebook, projectId);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.createNotebook(notebook, projectId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, notebook.getId());
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + notebook.getId()))
                .headers(headers).body(notebook);
    }

    /**
     * PUT  /notebooks/:id -> Updates notebook according to User permissions
     */
    @RequestMapping(value = PARENT_PATH_ID, method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> updateNotebook(@PathVariable String projectId,
                                                      @RequestBody @Valid NotebookDTO notebook) {
        log.debug("REST request to update notebook: {}", notebook);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.updateNotebook(notebook, projectId, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, notebook.getId());
        return ResponseEntity.ok().headers(headers).body(notebook) ;
    }

    /**
     * DELETE  /notebooks/:id -> Removes notebook with specified id
     */
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotebook(@PathVariable String projectId,
                                               @PathVariable String id) {
        log.debug("REST request to remove notebook: {}", id);
        notebookService.deleteNotebook(projectId, id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }

}