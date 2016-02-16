package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/notebooks/{notebookSequenceId:[\\d]+}/experiments";
    private static final String PATH_SEQ_ID = "/{sequenceId:[\\d]+}";
    private static final String ENTITY_NAME = "Experiment";

    private final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    /**
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments, which author is current User<br/> ?????????
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllExperimentsByPermissions(@PathVariable String notebookId) {
        User user = userService.getUserWithAuthorities();
        if (notebookId == null) {
            log.debug("REST request to get all experiments, which author is current user");
            Collection<ExperimentDTO> experiments = experimentService.getExperimentsByAuthor(user);
            return ResponseEntity.ok(experiments); //TODO May be use DTO only with required fields for Experiment?
        } else {
            log.debug("REST request to get all experiments of notebook: {} " +
                    "according to user permissions", notebookId);
            List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(notebookId, user);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /notebooks/:notebookId/experiments/all -> Returns all experiments of specified notebook
     * without checking for User permissions
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllExperiments(@PathVariable String notebookId) {
        log.debug("REST request to get all experiments of notebook: {} " +
                "without checking for permissions", notebookId);
        List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(notebookId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> getExperiment(
            @PathVariable String id) {
        log.debug("REST request to get experiment: {}", id);
        User user = userService.getUserWithAuthorities();
        return ResponseEntity.ok(experimentService.getExperiment(id, user));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> createExperiment(@RequestBody ExperimentDTO experimentDTO,
                                                          @PathVariable String notebookId)
            throws URISyntaxException {
        log.debug("REST request to create experiment: {} for notebook: {}", experimentDTO, notebookId);
        User user = userService.getUserWithAuthorities();
        experimentDTO = experimentService.createExperiment(experimentDTO, notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, experimentDTO.getId());
        return ResponseEntity.created(new URI("/api/notebooks/" + notebookId + "/experiments" + experimentDTO.getId()))
                .headers(headers).body(experimentDTO);
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> updateExperiment(@RequestBody  ExperimentDTO experimentDTO) {
        log.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        experimentDTO = experimentService.updateExperiment(experimentDTO, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, experimentDTO.getId());
        return ResponseEntity.ok().headers(headers).body(experimentDTO);
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteExperiment(@PathVariable String id, @PathVariable String notebookId) {
        log.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id, notebookId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }
}