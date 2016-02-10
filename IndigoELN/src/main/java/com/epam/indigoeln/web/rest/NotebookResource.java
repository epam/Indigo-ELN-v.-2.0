package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
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
import java.util.List;

@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/notebooks";
    private static final String ENTITY_NAME = "Notebook";

    private static final String PATH_SEQ_ID = "/{sequenceId:[\\d]+}";

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
            @RequestParam Long projectId,
            @RequestParam(required = false) String userId) {
        log.debug("REST request to get all notebooks of project: {} for user: {}", projectId, userId);
        User user = userService.getUserWithAuthorities();
        if (userId != null && !user.getId().equals(userId)) {
            // change executing user
            user = userService.getUserWithAuthorities(userId);
        }
        List<TreeNodeDTO> result = notebookService.getAllNotebookTreeNodes(projectId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id according to User permissions
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> getNotebook(@PathVariable Long sequenceId) {
        log.debug("REST request to get notebook: {}", sequenceId);
        User user = userService.getUserWithAuthorities();
        NotebookDTO notebook = notebookService.getNotebookById(sequenceId, user);
        return ResponseEntity.ok(notebook);
    }

    /**
     * POST  /notebooks?:projectId -> Creates notebook with OWNER's permissions for current User
     * as child for specified Project
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> createNotebook(@RequestBody @Valid NotebookDTO notebook,
                                   @RequestParam Long projectId) throws URISyntaxException {
        log.debug("REST request to create notebook: {} for project: {}", notebook, projectId);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.createNotebook(notebook, projectId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, notebook.getSequenceId().toString());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + notebook.getSequenceId()))
                .headers(headers).body(notebook);
    }

    /**
     * PUT  /notebooks/:id -> Updates notebook according to User permissions
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotebookDTO> updateNotebook(@RequestBody @Valid NotebookDTO notebook) {
        log.debug("REST request to update notebook: {}", notebook);
        User user = userService.getUserWithAuthorities();
        notebook = notebookService.updateNotebook(notebook, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, notebook.getSequenceId().toString());
        return ResponseEntity.ok().headers(headers).body(notebook) ;
    }

    /**
     * DELETE  /notebooks/:id -> Removes notebook with specified id
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteNotebook(@PathVariable Long sequenceId) {
        log.debug("REST request to remove notebook: {}", sequenceId);
        notebookService.deleteNotebook(sequenceId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, sequenceId.toString());
        return ResponseEntity.ok().headers(headers).build();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Void> notebookAlreadyExists() {
        HttpHeaders headers = HeaderUtil.createErrorAlert("Notebook name is already in use", ENTITY_NAME);
        return ResponseEntity.badRequest().headers(headers).build();
    }
}