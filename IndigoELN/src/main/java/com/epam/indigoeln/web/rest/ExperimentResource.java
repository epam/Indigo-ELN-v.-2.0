package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/notebooks/";

    private final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    CustomDtoMapper dtoMapper;

    /**
     * GET  /experiments -> Returns all experiments, which author is current User<br/>
     * GET  /experiments?:notebookId -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions<br/>
     * GET  /experiments?:notebookId&:userId -> Returns all experiments of specified notebook for <b>specified user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(value = "{notebookId}/experiments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllExperiments(
            @PathVariable String notebookId,
            @RequestParam(required = false) String userId) {
        User user = userService.getUserWithAuthorities();
        if (notebookId == null) {
            log.debug("REST request to get all experiments, which author is current user");
            Collection<Experiment> experiments = experimentService.getExperimentsByAuthor(user);
            return ResponseEntity.ok(experiments); //TODO May be use DTO only with required fields for Experiment?
        } else {
            log.debug("REST request to get all experiments of notebook: {} for user: {}", notebookId, userId);
            if (userId != null && !user.getId().equals(userId)) {
                // change executing user
                user = userService.getUserWithAuthorities(userId);
            }
            Collection<Experiment> experiments = experimentService.getAllExperiments(notebookId, user);

            List<TreeNodeDTO> result = new ArrayList<>(experiments.size());
            for (Experiment experiment : experiments) {
                ExperimentDTO experimentDTO = dtoMapper.convertToDTO(experiment);
                result.add(new TreeNodeDTO(experimentDTO));
            }
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @RequestMapping(value = "{notebookId}/experiments/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> getExperiment(
            @PathVariable String notebookId,
            @PathVariable String id) {
        log.debug("REST request to get experiment: {}", id);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = experimentService.getExperiment(id, user);
        return ResponseEntity.ok(dtoMapper.convertToDTO(experiment));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @RequestMapping(
            value = "{notebookId}/experiments",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> createExperiment(@RequestBody ExperimentDTO experimentDTO,
                                                          @PathVariable String notebookId) throws URISyntaxException {
        log.debug("REST request to create experiment: {} for notebook: {}", experimentDTO, notebookId);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        experiment = experimentService.createExperiment(experiment, notebookId, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + experiment.getId()))
                .body(dtoMapper.convertToDTO(experiment));
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions
     */
    @RequestMapping(
            value = "{notebookId}/experiments",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> updateExperiment(@RequestBody ExperimentDTO experimentDTO, @PathVariable String notebookId) {
        log.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        experiment = experimentService.updateExperiment(experiment, user);
        return ResponseEntity.ok(dtoMapper.convertToDTO(experiment));
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @RequestMapping(value = "{notebookId}/experiments/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteExperiment(@PathVariable String id, @PathVariable String notebookId) {
        log.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id, notebookId);
        return ResponseEntity.ok().build();
    }
}