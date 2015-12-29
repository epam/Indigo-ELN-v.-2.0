package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.web.model.security.CustomUserDetails;
import com.epam.indigoeln.web.model.security.UserInfo;
import com.epam.indigoeln.web.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/service")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @RequestMapping(value = "/createNewProject", method = RequestMethod.POST, consumes = "application/json")
    public void createNewProject(@RequestBody String projectData) {
        Project newProject = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            newProject = mapper.readValue(projectData, Project.class);

            for (String userName : newProject.getUsers()) {
                CustomUserDetails userDetails = (CustomUserDetails)userDetailsService.loadUserByUsername(userName);

                if (!(userDetails.isEnabled() && userDetails.isAccountNonLocked())) {
                    throw new Exception("User " + userName + " have no or no enough rights to work with this project.");
                }
            }

            projectService.saveProject(newProject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
