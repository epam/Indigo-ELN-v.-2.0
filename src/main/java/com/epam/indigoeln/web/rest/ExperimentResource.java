package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentTablesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ExperimentResource {

    private final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

	@Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/experiments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Experiment> getAllExperiments() {
        return experimentService.findByAuthor(userService.getUserWithAuthorities());
    }

    @RequestMapping(value = "/experiments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment getExperiment(@PathVariable String id) {
        return experimentService.findOne(id);
    }

    @RequestMapping(value = "/experiments",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment saveExperiment(@RequestBody Experiment experiment) {
        return experimentService.save(experiment);
    }

    /* Tables with experiments on the home page, where current user is an author, witness, or signature requested */
    @RequestMapping(value = "/experiments/tables",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentTablesDTO getExperimentTables() {
        User user = userService.getUserWithAuthorities();
        ExperimentTablesDTO dto = new ExperimentTablesDTO();

        Collection<Experiment> userExperiments = experimentService.findByAuthor(user);
        Collection<Experiment> allExperiments = experimentService.findAll();

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
