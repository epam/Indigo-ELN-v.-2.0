package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.auth.CustomUserDetails;
import com.epam.indigoeln.service.Experiment;
import com.epam.indigoeln.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@EnableAutoConfiguration
@RequestMapping("/service")
public class ExperimentController {

	@Autowired
	ExperimentService service;

    @RequestMapping("experiments")
    @ResponseBody
    List<Experiment> getExperiments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        //TODO: filter by current user
        return service.getExperiments().collect(Collectors.toList());
    }
}
