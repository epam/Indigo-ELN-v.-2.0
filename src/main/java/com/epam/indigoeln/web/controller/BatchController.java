package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.service.batch.BatchService;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
public class BatchController {

    @Autowired
    private BatchService batchService;

    @RequestMapping(value = "/experiments/{experimentId}/batches/save",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Batch saveBatch(@PathVariable String experimentId,
                           @RequestBody Batch batch) {
        return batchService.saveBatch(experimentId, batch);
    }

    @RequestMapping(value = "/experiments/{experimentId}/batches/{batchNumber}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteBatch(@PathVariable String experimentId,
                            @PathVariable String batchNumber) {
        batchService.deleteBatch(experimentId, batchNumber);
    }
}
