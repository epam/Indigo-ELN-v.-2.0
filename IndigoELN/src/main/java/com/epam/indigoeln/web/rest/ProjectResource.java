package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectResource {

    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/createNewProject", method = RequestMethod.POST, consumes = "application/json")
    public void createNewProject(@RequestBody Project project) {
        projectService.saveProject(project);
    }
}
