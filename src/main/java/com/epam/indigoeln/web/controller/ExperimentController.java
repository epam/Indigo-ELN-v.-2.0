package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.web.dto.ExperimentTablesDTO;
import com.epam.indigoeln.web.model.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/service")
public class ExperimentController {

    private final Logger log = LoggerFactory.getLogger(ExperimentController.class);

	@Autowired
    private ExperimentService experimentService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @RequestMapping(value = "/experiments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Experiment> getAllExperiments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        //TODO: filter by current user
        return experimentRepository.getAllExperiments();
    }

    @RequestMapping(value = "/experiments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment getExperiment(@PathVariable String id) {
        return experimentRepository.getExperiment(id);
    }

    @RequestMapping(value = "/experiments",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment saveExperiment(@RequestBody Experiment experiment) {
        return experimentRepository.saveExperiment(experiment);
    }

    /* Tables with experiments on the home page, current user is not an author of all experiments in the list */
    @RequestMapping(value = "/experiments/tables",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentTablesDTO getExperimentsList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return experimentService.getExperimentTables();
    }


}
