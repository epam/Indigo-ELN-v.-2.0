package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.component.ComponentService;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/experiments")
public class ComponentResource {

    @Autowired
    private ComponentService componentService;

    /**
     * PUT  /:experimentId/components/ -> add new component
     */
    @RequestMapping(value = "/{experimentId}/components",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<ComponentDTO> createComponent(@PathVariable String experimentId,
                                                @RequestBody ComponentDTO componentDTO) {
        return componentService.createComponent(experimentId, componentDTO).
                    map(ResponseEntity::ok).
                    orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * PUT  /:experimentId/components/:componentId -> update existing component
     */
    @RequestMapping(value = "/{experimentId}/components",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<ComponentDTO> updateComponent(@PathVariable String experimentId,
                                                @RequestBody ComponentDTO componentDTO) {
        if(!componentService.getComponent(experimentId, componentDTO.getId()).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return componentService.updateComponent(experimentId, componentDTO).
                    map(ResponseEntity::ok).
                    orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * DELETE  /:experimentId/components/:componentNumber -> delete component
     */
    @RequestMapping(value = "/{experimentId}/components/{componentId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public ResponseEntity<Void> deleteComponent(@PathVariable String experimentId,
                                            @PathVariable String componentId) {
        componentService.deleteComponent(experimentId, componentId);
        return ResponseEntity.ok().headers(
                    HeaderUtil.createAlert("A Component is deleted with identifier " + componentId, componentId)).build();
    }

    /**
     * GET  /:experimentId/components/:componentId -> get component details
     */
    @RequestMapping(value = "/{experimentId}/components/{componentId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<ComponentDTO> getComponent(@PathVariable  String experimentId,
                                                @PathVariable  String componentId) {
        return componentService.getComponent(experimentId, componentId).
                map(ResponseEntity::ok).
                orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /:experimentId/components -> get all components of given experiment
     */
    @RequestMapping(value = "/{experimentId}/components",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public ResponseEntity<List<ComponentDTO>> getAllExperimentComponents(@PathVariable  String experimentId) {
        return componentService.getAllExperimentComponents(experimentId).
                map(ResponseEntity::ok).
                orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
