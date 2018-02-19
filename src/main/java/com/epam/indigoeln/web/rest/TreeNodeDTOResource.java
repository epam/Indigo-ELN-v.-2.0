package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.notebook.NotebookService;
import com.epam.indigoeln.core.service.project.ProjectService;
import com.epam.indigoeln.web.rest.dto.ExperimentTreeNodeDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class TreeNodeDTOResource {

    static final String URL_MAPPING = "/api/tree";
    private static final String PATH_ID = "/{id:[\\d]+}";
    private static final String PROJECT_PATH_ID = "projects/{projectId:[\\d]+}";
    private static final String NOTEBOOK_PATH_ID = PROJECT_PATH_ID + "/notebooks/{id:[\\d]+}";
    private static final String EXPERIMENT_PATH_ID = NOTEBOOK_PATH_ID + "/experiments/{id:[\\d]+}";
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeNodeDTOResource.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private ExperimentService experimentService;

    /**
     * GET  /projects/project/:id -> Returns project with specified id according to User permissions.
     *
     * @param id Identifier
     * @return Project
     */
    @ApiOperation(value = "Returns project by it's id as tree node.")
    @RequestMapping(value = "/project" + PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TreeNodeDTO> getProjectForTree(
            @ApiParam("Project id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get project tree node: {}", id);
        TreeNodeDTO project = projectService.getProjectAsTreeNode(id);
        return ResponseEntity.ok(project);
    }

    /**
     * GET  /notebooks/notebook/:id -> Returns notebook with specified id for tree.
     *
     * @param id Notebook id
     * @return Returns notebook with specified id
     */
    @ApiOperation(value = "Returns notebook by it's id for tree.")
    @RequestMapping(value = NOTEBOOK_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TreeNodeDTO> getNotebookAsTree(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get notebook: {}", id);
        TreeNodeDTO notebook = notebookService.getNotebookAsTreeNode(projectId, id);
        return ResponseEntity.ok(notebook);
    }


    @ApiOperation(value = "Returns experiment with specified id for tree.")
    @RequestMapping(value = EXPERIMENT_PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentTreeNodeDTO> getExperimentAsTreeNode(
            @ApiParam("Project id") @PathVariable String projectId,
            @ApiParam("Notebook id") @PathVariable String notebookId,
            @ApiParam("Experiment id") @PathVariable String id
    ) {
        ExperimentTreeNodeDTO experimentTreeNodeDTO = experimentService
                .getExperimentAsTreeNode(projectId, notebookId, id);
        return ResponseEntity.ok(experimentTreeNodeDTO);
    }
}