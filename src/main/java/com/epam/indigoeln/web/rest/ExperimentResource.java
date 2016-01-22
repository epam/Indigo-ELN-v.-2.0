package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTablesDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/experiments";

    private final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    /**
     * GET  /experiments -> Returns all experiments, which author is current User, according to User permissions<br/>
     * GET  /experiments?:notebookId -> Returns all experiments of specified notebook
     * for tree representation according to User permissions
     * <p>
     * If User has <b>ADMIN</b> authority, than all experiments for specified notebook have to be returned
     * </p>
     */
    @RequestMapping(method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<?> getAllExperiments(
            @RequestParam(value = "notebookId", required = false) String notebookId) {
        if (notebookId == null) {
            log.debug("REST request to get all experiments of current User");
            Collection<Experiment> experiments = experimentService.getExperimentsByAuthor(
                    userService.getUserWithAuthorities());
            return ResponseEntity.ok(experiments);
        } else {
            log.debug("REST request to get all experiments of notebook: {}", notebookId);
            User user = userService.getUserWithAuthorities();
            Collection<Experiment> experiments = experimentService.getAllExperiments(notebookId, user);

            List<ExperimentTreeNodeDTO> result = new ArrayList<>(experiments.size());
            for (Experiment experiment : experiments) {
                ExperimentDTO experimentDTO = new ExperimentDTO(experiment);
                ExperimentTreeNodeDTO dto = new ExperimentTreeNodeDTO(experimentDTO);
                dto.setNodeType("experiment");
                result.add(dto);
            }
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<ExperimentDTO> getExperiment(@PathVariable("id") String id) {
        log.debug("REST request to get experiment: {}", id);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = experimentService.getExperiment(id, user);
        return ResponseEntity.ok(new ExperimentDTO(experiment));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @RequestMapping(method = RequestMethod.POST)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<ExperimentDTO> createExperiment(@RequestBody ExperimentDTO experimentDTO,
                                     @RequestParam(value = "notebookId") String notebookId) throws URISyntaxException {
        log.debug("REST request to create experiment for notebook: {}", experimentDTO, notebookId);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = ConverterUtils.convertFromDTO(experimentDTO);
        experiment = experimentService.createExperiment(experiment, notebookId, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + experiment.getId())).body(new ExperimentDTO(experiment));
    }

    /**
     * PUT  /experiments/:id -> Updates experiment with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<ExperimentDTO> updateExperiment(@PathVariable("id") String id,
                                                          @RequestBody ExperimentDTO experimentDTO) {
        log.debug("REST request to update experiment: {}", id, experimentDTO);
        User user = userService.getUserWithAuthorities();
        Experiment experiment = ConverterUtils.convertFromDTO(experimentDTO);
        experiment.setId(id);
        experiment = experimentService.updateExperiment(experiment, user);
        return ResponseEntity.ok(new ExperimentDTO(experiment));
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(AuthoritiesConstants.EXPERIMENT_REMOVER)
    public ResponseEntity<?> deleteExperiment(@PathVariable("id") String id) {
        log.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id);
        return ResponseEntity.ok(null);
    }

    /* Tables with experiments on the home page, where current user is an author, witness, or signature requested */
    @RequestMapping(value = "/tables",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentTablesDTO getExperimentTables() { //TODO Elena Polyakova: Convert to ResponseEntity<ExperimentTablesDTO>
        User user = userService.getUserWithAuthorities();
        ExperimentTablesDTO dto = new ExperimentTablesDTO();

        Collection<Experiment> userExperiments = experimentService.getExperimentsByAuthor(user);
        Collection<Experiment> allExperiments = experimentService.getAllExperiments();

        dto.setOpenAndCompletedExp(userExperiments.stream()
                .filter(exp -> exp.getStatus().equals("Open") || exp.getStatus().equals("Completed") || exp.getStatus().equals("Archived"))
                .collect(Collectors.toList()));
        dto.setSubmittedAndSigningExp(userExperiments.stream()
                .filter(exp -> exp.getStatus().equals("Submitted") || exp.getStatus().equals("Signing"))
                .collect(Collectors.toList()));
        dto.setWaitingSignatureExp(allExperiments.stream()
                .filter(exp -> exp.getCoAuthors().contains(user) /* || exp.getAuthor().equals(user) && in template for signing indicated that author's signature is required*/)
                .collect(Collectors.toList()));
        return dto;
    }
}