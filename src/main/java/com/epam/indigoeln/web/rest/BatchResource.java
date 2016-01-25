package com.epam.indigoeln.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.epam.indigoeln.core.service.batch.BatchService;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.core.security.AuthoritiesConstants;

import java.util.List;


@RestController
@RequestMapping("/api/experiments")
public class BatchResource {

    private final Logger log = LoggerFactory.getLogger(BatchResource.class);

    @Autowired
    private BatchService batchService;

    /**
     * PUT  /:experimentId/batches/ -> add new batch
     */
    @RequestMapping(value = "/{experimentId}/batches",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<BatchDTO> createBatch(@PathVariable String experimentId,
                                                @RequestBody  BatchDTO batchDTO) {
        log.debug("REST request to create Batch : {}", batchDTO);
        return batchService.createBatch(experimentId, batchDTO).
                    map(ResponseEntity::ok).
                    orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * PUT  /:experimentId/batches/:batchId -> update existing batch
     */
    @RequestMapping(value = "/{experimentId}/batches",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<BatchDTO> updateBatch(@PathVariable String experimentId,
                                                @RequestBody  BatchDTO batchDTO) {
        log.debug("REST request to update Batch : {}", batchDTO);
        if(!batchService.getBatch(experimentId, batchDTO.getId()).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return batchService.updateBatch(experimentId, batchDTO).
                    map(ResponseEntity::ok).
                    orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * DELETE  /:experimentId/batches/:batchNumber -> delete batch
     */
    @RequestMapping(value = "/{experimentId}/batches/{batchId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<Void> deleteBatch(@PathVariable String experimentId,
                                            @PathVariable String batchId) {
        log.debug("REST request to delete Batch with id: {} in experiment : {}", batchId, experimentId);
        batchService.deleteBatch(experimentId, batchId);
        return ResponseEntity.ok().headers(
                    HeaderUtil.createAlert("A Batch is deleted with identifier " + batchId, batchId)).build();
    }

    /**
     * GET  /:experimentId/batches/:batchId -> get batch details
     */
    @RequestMapping(value = "/{experimentId}/batches/{batchId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<BatchDTO> getBatch(@PathVariable  String experimentId,
                                                @PathVariable  String batchId) {
        log.debug("REST request to get Batch details for batch with id: {}", batchId);
        return batchService.getBatch(experimentId, batchId).
                map(ResponseEntity::ok).
                orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /:experimentId/batches -> get all batches of given experiment
     */
    @RequestMapping(value = "/{experimentId}/batches",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<List<BatchDTO>> getAllExperimentBatches(@PathVariable  String experimentId) {
        log.debug("REST request to get all batches for experiment with id: {}", experimentId);
        return batchService.getAllExperimentBatches(experimentId).
                map(ResponseEntity::ok).
                orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
