/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
     * GET  /notebooks/:notebookId/experiments/notebook-summary ->
     * Returns all experiments of specified notebook for <b>current user</b>.
     * for tree representation according to his User permissions.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @return Returns all experiments, or experiments for specified notebook, which author is current user
     */
    @ApiOperation(value = "Returns all experiments, "
            + "or experiments for specified notebook, which author is current user.")
    @RequestMapping(value = "/notebook-summary", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExperimentDTO>> getAllExperimentsForNotebookSummary(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
    ) {
        User user = userService.getUserWithAuthorities();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all experiments of notebook: {} "
                    + "according to user permissions", notebookId);
        }
        List<ExperimentDTO> result = experimentService.getAllExperimentNotebookSummary(projectId, notebookId, user);
        return ResponseEntity.ok(result);
    }



    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @param id         Experiment id
     * @return Returns experiment with specified id according to User permissions
     * @throws IOException if there is a low-level I/O problem in experiment status
     */
    @ApiOperation(value = "Returns experiment with specified id according to User permissions.")
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
        experimentDTO.setStatus(signatureService.updateAndGetExperimentStatus(experimentDTO));
        return ResponseEntity.ok(experimentDTO);
    }

    /**
     * GET  /experiments/:id/batch_number -> Returns batch_number.
     *
     * @param projectId               Project id
     * @param notebookId              Notebook id
     * @param id                      Experiment id
     * @param clientLatestBatchNumber If latest number should be returned
     * @return Returns batch_number
     */
    @ApiOperation(value = "Returns batch number.")
    @RequestMapping(value = PATH_ID + "/batch_number", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getNotebookBatchNumber(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id,
            @ApiParam("If latest number should be returned")
            @RequestParam(required = false, name = "latest") String clientLatestBatchNumber
    ) {
        String batchNumber = sequenceIdService.getNotebookBatchNumber(projectId, notebookId,
                id, clientLatestBatchNumber);
        LOGGER.debug("REST request to get batch number: {}", batchNumber);
        return ResponseEntity.ok(ImmutableMap.of("batchNumber", batchNumber));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook.
     *
     * @param experimentDTO Experiment to create
     * @param projectId     Project id
     * @param notebookId    Notebook id
     * @return Created experiment
     * @throws URISyntaxException Id URI is not correct
     */
    @ApiOperation(value = "Creates experiment with OWNER's permissions for current user.")
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
        ExperimentDTO savedExperimentDTO = experimentService.createExperiment(experimentDTO, projectId,
                notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, null);
        return ResponseEntity.created(new URI("/api/projects/" + projectId
                + "/notebooks/" + notebookId + "/experiments" + experimentDTO.getId()))
                .headers(headers).body(savedExperimentDTO);
    }

    /**
     * POST  /experiments/version?:notebookId -> Creates experiment version with OWNER's permissions for current User
     * as child for specified Notebook.
     *
     * @param experimentName Experiment name
     * @param projectId      Project id
     * @param notebookId     Notebook id
     * @return Created experiment
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Creates experiment version with OWNER's permissions for current user.")
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
        LOGGER.debug("REST request to create a version of an experiment: {} for notebook: {}",
                experimentName, notebookId);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO createdExperimentDTO = experimentService.versionExperiment(experimentName,
                projectId, notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, null);
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + notebookId
                + "/experiments" + createdExperimentDTO.getId()))
                .headers(headers).body(createdExperimentDTO);
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions.
     *
     * @param experimentDTO Experiment to update
     * @param projectId     Project id
     * @param notebookId    Notebook id
     * @return Updated experiment
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates experiment according to permissions.")
    public ResponseEntity<ExperimentDTO> updateExperiment(
            @ApiParam("Experiment to update") @RequestBody ExperimentDTO experimentDTO,
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId
    ) {
        LOGGER.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO updatedExperimentDTO = experimentService
                .updateExperiment(projectId, notebookId, experimentDTO, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).body(updatedExperimentDTO);
    }

    /**
     * PUT  /experiments/:id -> reopen experiment according to User permissions.
     *
     * @param version    Experiment version
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @param id         Experiment id
     * @return Experiment
     */
    @RequestMapping(
            value = PATH_ID + "/reopen",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reopen experiment according to permissions.")
    public ResponseEntity<ExperimentDTO> reopenExperiment(
            @ApiParam("Experiment version") @RequestBody Long version,
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to reopen experiment: {}", SequenceIdUtil.buildFullId(projectId, notebookId, id));
        User user = userService.getUserWithAuthorities();
        ExperimentDTO updatedExperimentDTO = experimentService
                .reopenExperiment(projectId, notebookId, id, version, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).body(updatedExperimentDTO);
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id.
     *
     * @param projectId  Project id
     * @param notebookId Notebook id
     * @param id         Experiment id
     */
    @ApiOperation(value = "Removes experiment.")
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity deleteExperiment(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id, projectId, notebookId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }
}
