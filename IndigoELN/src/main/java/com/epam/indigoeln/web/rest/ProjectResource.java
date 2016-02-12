package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping(ProjectResource.URL_MAPPING)
public class ProjectResource {

    static final String URL_MAPPING = "/api/projects";
    private static final String PATH_SEQ_ID = "/{sequenceId:[\\d]+}";
    private static final String ENTITY_NAME = "Project";

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    /**
     * GET  /projects -> Returns all projects for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjectsByPermissions() {
        log.debug("REST request to get all projects according to user permissions");
        User user = userService.getUserWithAuthorities();
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes(user);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/all -> Returns all projects without checking for User permissions
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllProjects() {
        log.debug("REST request to get all projects without checking for permissions");
        List<TreeNodeDTO> result = projectService.getAllProjectsAsTreeNodes();
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Long sequenceId) {
        log.debug("REST request to get project: {}", sequenceId);
        User user = userService.getUserWithAuthorities();
        ProjectDTO project = projectService.getProjectById(sequenceId, user);
        return ResponseEntity.ok(project);
    }

    /**
     * POST  /projects -> Creates project with OWNER's permissions for current User
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO project) throws URISyntaxException {
        log.debug("REST request to create project: {}", project);
        User user = userService.getUserWithAuthorities();
        project = projectService.createProject(project, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, project.getSequenceId().toString());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + project.getSequenceId()))
                .headers(headers).body(project);
    }

    /**
     * PUT  /projects -> Updates project according to User permissions
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDTO> updateProject(@RequestBody ProjectDTO project) {
        log.debug("REST request to update project: {}", project);
        User user = userService.getUserWithAuthorities();
        project = projectService.updateProject(project, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, project.getSequenceId().toString());
        return ResponseEntity.ok().headers(headers).body(project);
    }

    /**
     * DELETE  /projects/:id -> Removes project with specified id
     */
    @RequestMapping(value = PATH_SEQ_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteProject(@PathVariable Long sequenceId) {
        log.debug("REST request to remove project: {}", sequenceId);
        projectService.deleteProject(sequenceId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, sequenceId.toString());
        return ResponseEntity.ok().headers(headers).build();
    }
}