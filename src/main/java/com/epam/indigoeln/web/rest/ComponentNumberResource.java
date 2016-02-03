package com.epam.indigoeln.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.component.number.ComponentNumberService;


@RestController
@RequestMapping("/api/")
public class ComponentNumberResource {

    @Autowired
    private ComponentNumberService componentNumberService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @RequestMapping(value = "experiments/{experimentId}/batches/number/next",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateNextBatchNumber(@PathVariable String experimentId) {
        if(!experimentRepository.exists(experimentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(componentNumberService.generateNextBatchNumber(experimentId));
    }

    @RequestMapping(value = "experiments/{experimentId}/batches/number/{number}/exists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isBatchNumberExists(@PathVariable String experimentId,
                                                       @PathVariable String number) {
        if(!experimentRepository.exists(experimentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(componentNumberService.isBatchNumberExists(experimentId, number));
    }

    @RequestMapping(value = "projects/{projectId}/experiments/number/next",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateNextExperimentNumber(@PathVariable String projectId) {
        if(!projectRepository.exists(projectId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(componentNumberService.generateNextExperimentNumber(projectId));
    }

    @RequestMapping(value = "projects/{projectId}/experiments/number/{number}/exists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isExperimentNumberExists(@PathVariable String projectId,
                                                            @PathVariable String number) {
        if(!projectRepository.exists(projectId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(componentNumberService.isExperimentNumberExists(projectId, number));
    }
}
