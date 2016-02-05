package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(ProjectResource.URL_MAPPING)
public class ProjectResource {

    static final String URL_MAPPING = "/api/projects";

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private UserService userService;

    /**
     * GET  /projects -> Returns all projects for <b>current user</b>
     * for tree representation according to his User permissions<br/>
     * GET  /projects?:userId -> Returns all projects for <b>specified user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjects(
            @RequestParam(required = false) String userId) {
        log.debug("REST request to get all projects for user: {}", userId);
        User user = userService.getUserWithAuthorities();
        if (userId != null && !user.getId().equals(userId)) {
            // change executing user
            user = userService.getUserWithAuthorities(userId);
        }
        Collection<Project> projects = projectService.getAllProjects(user);
        List<TreeNodeDTO> result = new ArrayList<>(projects.size());
        for (Project project : projects) {
            TreeNodeDTO dto = new TreeNodeDTO(project);
            dto.setHasChildren(notebookService.hasNotebooks(project, user));
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Project> getProject(@PathVariable String id) {
        log.debug("REST request to get project: {}", id);
        User user = userService.getUserWithAuthorities();
        Project project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(project);
    }

    /**
     * POST  /projects -> Creates project with OWNER's permissions for current User
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Project> createProject(@RequestBody Project project) throws URISyntaxException {
        log.debug("REST request to create project: {}", project);
        User user = userService.getUserWithAuthorities();
        project = projectService.createProject(project, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + project.getId())).body(project);
    }

    /**
     * PUT  /projects -> Updates project according to User permissions
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Project> updateProject(@RequestBody Project project) {
        log.debug("REST request to update project: {}", project);
        User user = userService.getUserWithAuthorities();
        project = projectService.updateProject(project, user);
        return ResponseEntity.ok(project);
    }

    /**
     * DELETE  /projects/:id -> Removes project with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        log.debug("REST request to remove project: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}