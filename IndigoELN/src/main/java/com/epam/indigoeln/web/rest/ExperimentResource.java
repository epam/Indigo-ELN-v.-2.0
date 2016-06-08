package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/projects/{projectId:[\\d]+}/notebooks/{notebookId:[\\d]+}/experiments";
    private static final String PATH_ID = "/{id:[\\d_]+}";
    private static final String ENTITY_NAME = "Experiment";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private UserService userService;

    @Autowired
    private SequenceIdService sequenceIdService;

    /**
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments, which author is current User<br/> ?????????
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @ApiOperation(value = "Returns all experiments, or experiments for specified notebook, which author is current user.", produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllExperimentsByPermissions(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
        ) {
        User user = userService.getUserWithAuthorities();
        if (notebookId == null) {
            LOGGER.debug("REST request to get all experiments, which author is current user");
            Collection<ExperimentDTO> experiments = experimentService.getExperimentsByAuthor(user);
            return ResponseEntity.ok(experiments); //TODO May be use DTO only with required fields for Experiment?
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("REST request to get all experiments of notebook: {} " +
                        "according to user permissions", notebookId);
            }
            List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(projectId, notebookId, user);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /notebooks/:notebookId/experiments/all -> Returns all experiments of specified notebook
     * without checking for User permissions
     */
    @ApiOperation(value = "Returns all experiments of specified notebook for current user for tree representation", produces = "application/json")
    @RequestMapping(value = "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllExperiments(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all experiments of notebook: {} " +
                    "without checking for permissions", notebookId);
        }
        List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(projectId, notebookId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @ApiOperation(value = "Returns experiment with specified id according to User permissions.", produces = "application/json")
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> getExperiment(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id
        ) throws IOException {
        LOGGER.debug("REST request to get experiment: {}", id);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO experimentDTO = experimentService.getExperiment(projectId, notebookId, id, user);
        experimentDTO.setStatus(signatureService.checkExperimentStatus(experimentDTO));
        return ResponseEntity.ok(experimentDTO);
    }

    /**
     * GET  /experiments/:id/batch_number -> Returns batch_number
     */
    @ApiOperation(value = "Returns batch number.", produces = "application/json")
    @RequestMapping(value = PATH_ID + "/batch_number", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> getNotebookBatchNumber(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id,
            @ApiParam("If latest number should be returned") @RequestParam(required = false, name = "latest") String clientLatestBatchNumber
        ) {
        String batchNumber = sequenceIdService.getNotebookBatchNumber(projectId, notebookId, id, clientLatestBatchNumber);
        LOGGER.debug("REST request to get batch number: {}", batchNumber);
        return ResponseEntity.ok(ImmutableMap.of("batchNumber", batchNumber));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @ApiOperation(value = "Creates experiment with OWNER's permissions for current user.", produces = "application/json")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> createExperiment(
            @ApiParam("Experiment to create") @RequestBody ExperimentDTO experimentDTO,
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
        ) throws URISyntaxException {
        LOGGER.debug("REST request to create experiment: {} for notebook: {}", experimentDTO, notebookId);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO savedExperimentDTO = experimentService.createExperiment(experimentDTO, projectId, notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, savedExperimentDTO.getId());
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + notebookId + "/experiments" + experimentDTO.getId()))
                .headers(headers).body(savedExperimentDTO);
    }

    /**
     * POST  /experiments/version?:notebookId -> Creates experiment version with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @ApiOperation(value = "Creates experiment version with OWNER's permissions for current user.", produces = "application/json")
    @RequestMapping(
            path = "/version",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> versionExperiment(
            @ApiParam("Experiment name") @RequestBody String experimentName,
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
        ) throws URISyntaxException {
        LOGGER.debug("REST request to create a version of an experiment: {} for notebook: {}", experimentName, notebookId);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO createdExperimentDTO = experimentService.versionExperiment(experimentName, projectId, notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, createdExperimentDTO.getId());
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + notebookId + "/experiments" + createdExperimentDTO.getId()))
                .headers(headers).body(createdExperimentDTO);
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates experiment according to permissions.", produces = "application/json")
    public ResponseEntity<ExperimentDTO> updateExperiment(
            @ApiParam("Experiment to update") @RequestBody ExperimentDTO experimentDTO,
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
        ) {
        LOGGER.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO updatedExperimentDTO = experimentService.updateExperiment(projectId, notebookId, experimentDTO, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedExperimentDTO.getId());
        return ResponseEntity.ok().headers(headers).body(updatedExperimentDTO);
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @ApiOperation(value = "Removes experiment.", produces = "application/json")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity deleteExperiment(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id
        ) {
        LOGGER.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id, projectId, notebookId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }

}