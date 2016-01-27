package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(NotebookResource.URL_MAPPING)
public class NotebookResource {

    static final String URL_MAPPING = "/api/notebooks";

    private final Logger log = LoggerFactory.getLogger(NotebookResource.class);

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    CustomDtoMapper dtoMapper;

    /**
     * GET  /notebooks?:projectId -> Returns all notebooks of specified project
     * for tree representation according to User permissions
     * <p>
     * If User has <b>ADMIN</b> authority, than all notebooks for specified project have to be returned
     * </p>
     */
    @RequestMapping(method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.NOTEBOOK_READER)
    public ResponseEntity<List<ExperimentTreeNodeDTO>> getAllNotebooks(
            @RequestParam(value = "projectId") String projectId) {
        if (true) {
            //stub
            List<ExperimentTreeNodeDTO> result = Lists.newArrayList();
            for (int i = 0; i < RandomUtils.nextInt(15) + 1; i++) {
                Notebook notebook = new Notebook();
                notebook.setId(RandomStringUtils.randomAlphanumeric(10));
                notebook.setName(RandomStringUtils.randomAlphanumeric(10));
                Experiment experiment = new Experiment();
                experiment.setId(RandomStringUtils.randomAlphanumeric(10));
                experiment.setTitle(RandomStringUtils.randomAlphabetic(10));
                notebook.setExperiments(ImmutableList.of(experiment));
                ExperimentTreeNodeDTO element = new ExperimentTreeNodeDTO(experiment);
                result.add(element);
            }
            return ResponseEntity.ok(result);
        }
        log.debug("REST request to get all notebooks of project: {}", projectId);
        User user = userService.getUserWithAuthorities();
        Collection<Notebook> notebooks = notebookService.getAllNotebooks(projectId, user);
        List<ExperimentTreeNodeDTO> result = new ArrayList<>(notebooks.size());
        for (Notebook notebook : notebooks) {
            NotebookDTO notebookDTO = dtoMapper.convertToDTO(notebook);
            ExperimentTreeNodeDTO dto = new ExperimentTreeNodeDTO(notebookDTO);
            dto.setNodeType("notebook");
            dto.setHasChildren(experimentService.hasExperiments(notebook, user));
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /notebooks/:id -> Returns notebook with specified id according to User permissions
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Secured(AuthoritiesConstants.NOTEBOOK_READER)
    public ResponseEntity<NotebookDTO> getNotebook(@PathVariable("id") String id) {
        log.debug("REST request to get notebook: {}", id);
        User user = userService.getUserWithAuthorities();
        Notebook notebook = notebookService.getNotebookById(id, user);
        return ResponseEntity.ok(dtoMapper.convertToDTO(notebook));
    }

    /**
     * POST  /notebooks?:projectId -> Creates notebook with OWNER's permissions for current User
     * as child for specified Project
     */
    @RequestMapping(method = RequestMethod.POST)
    @Secured(AuthoritiesConstants.NOTEBOOK_CREATOR)
    public ResponseEntity<NotebookDTO> createNotebook(@RequestBody NotebookDTO notebookDTO,
                                   @RequestParam(value = "projectId") String projectId) throws URISyntaxException {
        log.debug("REST request to create notebook: {} for project: {}", notebookDTO, projectId);
        User user = userService.getUserWithAuthorities();

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);
        notebook = notebookService.createNotebook(notebook, projectId, user);
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + notebook.getId()))
                .body(dtoMapper.convertToDTO(notebook));
    }

    /**
     * PUT  /notebooks/:id -> Updates notebook with specified id according to User permissions
     */
    @RequestMapping(value="/{id}", method = RequestMethod.PUT)
    @Secured(AuthoritiesConstants.NOTEBOOK_CREATOR)
    public ResponseEntity<NotebookDTO> updateNotebook(@PathVariable("id") String id, @RequestBody NotebookDTO notebookDTO) {
        log.debug("REST request to update notebook: {} with id: {}", notebookDTO, id);
        User user = userService.getUserWithAuthorities();

        Notebook notebook = dtoMapper.convertFromDTO(notebookDTO);
        notebook.setId(id);
        notebook = notebookService.updateNotebook(notebook, user);
        return ResponseEntity.ok(dtoMapper.convertToDTO(notebook));
    }

    /**
     * DELETE  /notebooks/:id -> Removes notebook with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Secured(AuthoritiesConstants.NOTEBOOK_REMOVER)
    public ResponseEntity<?> deleteNotebook(@PathVariable("id") String id) {
        log.debug("REST request to remove notebook: {}", id);
        notebookService.deleteNotebook(id);
        return ResponseEntity.ok(null);
    }
}