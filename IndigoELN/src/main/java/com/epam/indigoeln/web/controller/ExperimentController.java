package com.epam.indigoeln.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epam.indigoeln.service.Experiment;
import com.epam.indigoeln.service.ExperimentService;

@Controller
@EnableAutoConfiguration
@RequestMapping("/service")
public class ExperimentController {
	@Autowired
	ExperimentService service;

    @RequestMapping("experiments")
    @ResponseBody
    List<Experiment> getExperiments() {
        return service.getExperiments().collect(Collectors.toList());
    }
}
