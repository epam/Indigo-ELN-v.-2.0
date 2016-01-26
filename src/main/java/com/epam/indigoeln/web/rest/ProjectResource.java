package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.util.ConverterUtils;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * GET  /projects -> Returns all projects for tree representation according to User permissions
     * <p>
     * If User has <b>ADMIN</b> authority, than all projects have to be returned
     * </p>
     */
    @RequestMapping(method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.PROJECT_READER)
    public ResponseEntity<List<ExperimentTreeNodeDTO>> getAllProjects() {
        if (true) {
            List<ExperimentTreeNodeDTO> result = Lists.newArrayList();
            for (int i = 0; i < RandomUtils.nextInt(15) + 1; i++) {
                ProjectDTO projectDTO = new ProjectDTO();
                projectDTO.setId(RandomStringUtils.randomAlphanumeric(10));
                projectDTO.setName(RandomStringUtils.randomAlphanumeric(10));
                ExperimentTreeNodeDTO element = new ExperimentTreeNodeDTO(projectDTO);
                result.add(element);
            }
            return ResponseEntity.ok(result);
        }
        log.debug("REST request to get all projects");
        User user = userService.getUserWithAuthorities();
        Collection<Project> projects = projectService.getAllProjects(user);
        List<ExperimentTreeNodeDTO> result = new ArrayList<>(projects.size());
        for (Project project : projects) {
            ProjectDTO projectDTO = new ProjectDTO(project);
            ExperimentTreeNodeDTO dto = new ExperimentTreeNodeDTO(projectDTO);
            dto.setNodeType("project");
            dto.setHasChildren(notebookService.hasNotebooks(project, user));
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /projects/:id -> Returns project with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.PROJECT_READER)
    public ResponseEntity<ProjectDTO> getProject(@PathVariable("id") String id) {
        log.debug("REST request to get project: {}", id);
        User user = userService.getUserWithAuthorities();
        Project project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(new ProjectDTO(project));
    }

    /**
     * POST  /projects -> Creates project with OWNER's permissions for current User
     */
    @RequestMapping(method = RequestMethod.POST)
    @Secured(AuthoritiesConstants.PROJECT_CREATOR)
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) throws URISyntaxException {
        log.debug("REST request to create project: {}", projectDTO);
        User user = userService.getUserWithAuthorities();

        Project project = ConverterUtils.convertFromDTO(projectDTO);
        project = projectService.createProject(project, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + project.getId())).body(new ProjectDTO(project));
    }

    /**
     * PUT  /projects/:id -> Updates project with specified id according to User permissions
     */
    @RequestMapping(value="/{id}", method = RequestMethod.PUT)
    @Secured(AuthoritiesConstants.PROJECT_CREATOR)
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable("id") String id, @RequestBody ProjectDTO projectDTO) {
        log.debug("REST request to update project: {} with id: {}", projectDTO, id);
        User user = userService.getUserWithAuthorities();

        Project project = ConverterUtils.convertFromDTO(projectDTO);
        project.setId(id);
        project = projectService.updateProject(project, user);
        return ResponseEntity.ok(new ProjectDTO(project));
    }

    /**
     * DELETE  /projects/:id -> Removes project with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(AuthoritiesConstants.PROJECT_REMOVER)
    public ResponseEntity<?> deleteProject(@PathVariable("id") String id) {
        log.debug("REST request to remove project: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok(null);
    }

    // TODO move
    @RequestMapping(value = "/uploadfile",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> attachFile(@RequestParam MultipartFile file,
                                           @RequestParam String entityid,
                                           @RequestParam String entity) {
        log.debug("File for project : {}", entityid);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Project", entityid)).build();
    }
}