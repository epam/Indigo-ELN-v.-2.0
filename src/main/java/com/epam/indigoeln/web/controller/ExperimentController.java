package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
public class ExperimentController {

    private final Logger log = LoggerFactory.getLogger(ExperimentController.class);

	@Autowired
    private ExperimentService experimentService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private UserRepository userRepository;


    @RequestMapping(value = "/experiments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Experiment> getAllExperiments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.getUser(userDetails.getUserInfo().getUsername());
        return experimentRepository.findUserExperiments(user);
    }

    @RequestMapping(value = "/experiments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment getExperiment(@PathVariable String id) {
        return experimentRepository.findExperiment(id);
    }

    @RequestMapping(value = "/experiments",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Experiment saveExperiment(@RequestBody Experiment experiment) {
        return experimentRepository.saveExperiment(experiment);
    }

    /* Tables with experiments on the home page, where current user is an author, witness, or signature requested */
    @RequestMapping(value = "/experiments/tables",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentTablesDTO getExperimentTables(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.getUser(userDetails.getUserInfo().getUsername());
        ExperimentTablesDTO dto = new ExperimentTablesDTO();

        Collection<Experiment> userExperiments = experimentRepository.findUserExperiments(user);
        Collection<Experiment> allExperiments = experimentRepository.findAllExperiments();

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
