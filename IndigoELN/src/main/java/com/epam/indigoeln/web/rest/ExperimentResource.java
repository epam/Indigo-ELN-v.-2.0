package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/notebooks/{notebookSequenceId:[\\d]+}/experiments";
    private static final String PATH_SEQ_ID = "/{sequenceId:[\\d]+}";

    private final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    /**
     * GET  /experiments -> Returns all experiments, which author is current User<br/>
     * GET  /experiments?:notebookId -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions<br/>
     * GET  /experiments?:notebookId&:userId -> Returns all experiments of specified notebook for <b>specified user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllExperiments(
            @PathVariable Long notebookSequenceId,
            @RequestParam(required = false) String userId) {
        User user = userService.getUserWithAuthorities();
        if (notebookSequenceId == null) {
            log.debug("REST request to get all experiments, which author is current user");
            Collection<ExperimentDTO> experiments = experimentService.getExperimentsByAuthor(user);
            return ResponseEntity.ok(experiments); //TODO May be use DTO only with required fields for Experiment?
        } else {
            log.debug("REST request to get all experiments of notebook: {} for user: {}", notebookSequenceId, userId);
            if (userId != null && !user.getId().equals(userId)) {
                // change executing user
                user = userService.getUserWithAuthorities(userId);
            }
            Collection<ExperimentDTO> experiments = experimentService.getAllExperiments(notebookSequenceId, user);
            List<TreeNodeDTO> result = experiments.stream().map(TreeNodeDTO::new).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> getExperiment(
            @PathVariable Long notebookSequenceId,
            @PathVariable Long sequenceId) {
        log.debug("REST request to get experiment: {}", sequenceId);
        User user = userService.getUserWithAuthorities();
        return ResponseEntity.ok(experimentService.getExperiment(sequenceId, user));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> createExperiment(@RequestBody  ExperimentDTO experimentDTO,
                                                          @PathVariable Long notebookSequenceId)
            throws URISyntaxException {
        log.debug("REST request to create experiment: {} for notebook: {}", experimentDTO, notebookSequenceId);
        User user = userService.getUserWithAuthorities();
        experimentDTO = experimentService.createExperiment(experimentDTO, notebookSequenceId, user);
        return ResponseEntity.created(new URI("/api/notebooks/" + notebookSequenceId + "/experiments" + experimentDTO.getSequenceId()))
                .body(experimentDTO);
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> updateExperiment(@RequestBody  ExperimentDTO experimentDTO,
                                                          @PathVariable Long notebookSequenceId) {
        log.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        return ResponseEntity.ok(experimentService.updateExperiment(experimentDTO, user));
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteExperiment(@PathVariable Long sequenceId, @PathVariable Long notebookSequenceId) {
        log.debug("REST request to remove experiment: {}", sequenceId);
        experimentService.deleteExperiment(sequenceId, notebookSequenceId);
        return ResponseEntity.ok().build();
    }
}