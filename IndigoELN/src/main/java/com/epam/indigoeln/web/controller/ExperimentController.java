package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.web.model.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
public class ExperimentController {

	@Autowired
	private ExperimentService service;

    @RequestMapping("experiments")
    public List<Experiment> getExperiments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        //TODO: filter by current user
        return service.getExperiments().collect(Collectors.toList());
    }
}
