package com.epam.indigoeln.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.epam.indigoeln.core.service.batch.BatchService;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;


@RestController
@RequestMapping("/api/experiments")
public class BatchResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(BatchResource.class);

    @Autowired
    private BatchService batchService;

    /**
     * PUT  /:experimentId/batches/ -> add new batch
     */
    @RequestMapping(value = "/{experimentId}/batches",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BatchDTO> createBatch(@PathVariable String experimentId,
                                                @RequestBody  BatchDTO batchDTO) {
        LOGGER.debug("REST request to create Batch : {}", batchDTO);
        return ResponseEntity.ok(batchService.createBatch(experimentId, batchDTO));
    }

    /**
     * PUT  /:experimentId/batches/:batchId -> update existing batch
     */
    @RequestMapping(value = "/{experimentId}/batches",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BatchDTO> updateBatch(@PathVariable String experimentId,
                                                @RequestBody  BatchDTO batchDTO) {
        LOGGER.debug("REST request to update Batch : {}", batchDTO);
        if(!batchService.getBatch(experimentId, batchDTO.getId()).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return  ResponseEntity.ok(batchService.updateBatch(experimentId, batchDTO));
    }

    /**
     * DELETE  /:experimentId/batches/:batchNumber -> delete batch
     */
    @RequestMapping(value = "/{experimentId}/batches/{batchId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteBatch(@PathVariable String experimentId,
                                            @PathVariable String batchId) {
        LOGGER.debug("REST request to delete Batch with id: {} in experiment : {}", batchId, experimentId);
        batchService.deleteBatch(experimentId, batchId);
        return ResponseEntity.ok().headers(
                HeaderUtil.createAlert("A Batch is deleted with identifier " + batchId, batchId)).build();
    }
}
