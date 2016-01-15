package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.web.rest.dto.mapper.BatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.service.batch.BatchService;
import com.epam.indigoeln.web.rest.dto.BatchDTO;


@RestController
@RequestMapping("/api/experiments")
public class BatchResource {

    @Autowired
    private BatchService batchService;

    @Autowired
    private BatchMapper batchMapper;


    @RequestMapping(value = "/{experimentId}/batches/save",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public BatchDTO saveBatch(@PathVariable String experimentId,
                              @RequestBody BatchDTO batchDTO) {
        Batch batch = batchService.saveBatch(experimentId, batchMapper.batchFromBatchDTO(batchDTO));
        return batchMapper.batchDtoFromBatch(batch);
    }

    @RequestMapping(value = "/{experimentId}/batches/{batchNumber}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteBatch(@PathVariable String experimentId,
                            @PathVariable String batchNumber) {
        batchService.deleteBatch(experimentId, batchNumber);
    }
}
