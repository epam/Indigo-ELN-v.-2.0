package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/notebooks";

    private final Logger log = LoggerFactory.getLogger(NotebookResource.class);

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    /**
     * GET  /notebooks?:projectId -> Returns all notebooks of specified project for <b>current user</b>
     * for tree representation according to his User permissions<br/>
     * GET  /notebooks?:projectId&:userId -> Returns all notebooks of specified project for <b>specified user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllNotebooks(
            @RequestParam String projectId,
            @RequestParam(required = false) String userId) {
        log.debug("REST request to get all notebooks of project: {} for user: {}", projectId, userId);
        User user = userService.getUserWithAuthorities();
        if (userId != null && !user.getId().equals(userId)) {
            // change executing user
            user = userService.getUserWithAuthorities(userId);
        }
        Collection<Notebook> notebooks = notebookService.getAllNotebooks(projectId, user);
        List<TreeNodeDTO> result = new ArrayList<>(notebooks.size());
        for (Notebook notebook : notebooks) {
            TreeNodeDTO dto = new TreeNodeDTO(notebook);
            dto.setHasChildren(experimentService.hasExperiments(notebook, user));
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notebook> getNotebook(@PathVariable String id) {
        log.debug("REST request to get notebook: {}", id);
        User user = userService.getUserWithAuthorities();
        Notebook notebook = notebookService.getNotebookById(id, user);
        return ResponseEntity.ok(notebook);
    }

    /**
     * POST  /notebooks?:projectId -> Creates notebook with OWNER's permissions for current User
     * as child for specified Project
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notebook> createNotebook(@RequestBody @Valid Notebook notebook,
                                   @RequestParam String projectId) throws URISyntaxException {
        log.debug("REST request to create notebook: {} for project: {}", notebook, projectId);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.createNotebook(notebook, projectId, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + notebook.getId())).body(notebook);
    }

    /**
     * PUT  /notebooks/:id -> Updates notebook according to User permissions
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Notebook> updateNotebook(@RequestBody @Valid Notebook notebook) {
        log.debug("REST request to update notebook: {}", notebook);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.updateNotebook(notebook, user);
        return ResponseEntity.ok(notebook);
    }

    /**
     * DELETE  /notebooks/:id -> Removes notebook with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotebook(@PathVariable String id) {
        log.debug("REST request to remove notebook: {}", id);
        notebookService.deleteNotebook(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Void> notebookAlreadyExists() {
        HttpHeaders headers = HeaderUtil.createFailureAlert("notebook-management", "Notebook name is already in use");
        return ResponseEntity.badRequest().headers(headers).build();
    }
}