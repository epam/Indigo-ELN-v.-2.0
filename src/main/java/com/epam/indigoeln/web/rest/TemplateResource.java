package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.template.experiment.ExperimentTemplateRepository;
import com.epam.indigoeln.core.service.template.TemplateService;
import com.epam.indigoeln.web.rest.dto.ComponentTemplateDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTemplateDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing Resources
 */
@RestController
@RequestMapping("/api")
public class TemplateResource {

    private final static String WARNING_EXPERIMENTS_ASSIGNED = "Template with identifier %s could not be deleted : any assigned experiments exists.";
    private final static String WARNING_TEMPLATES_ASSIGNED = "Component template with identifier %s could not be deleted : any assigned templates exists.";

    @Autowired
    TemplateService templateService;

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    ExperimentTemplateRepository experimentTemplateRepository;

    /**
     * GET /templates/:id -> get template by id
     */
    @RequestMapping(value = "/templates/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentTemplateDTO> getTemplate(@PathVariable String id) {
        return templateService.getTemplateById(id)
                .map(template -> new ResponseEntity<>(template, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * GET /templates -> fetch all template list
     */
    @RequestMapping(value = "/templates",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExperimentTemplateDTO>> getAllTemplates(Pageable pageable)
            throws URISyntaxException {
        Page<ExperimentTemplateDTO> page = templateService.getAllTemplates(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/templates");
        return new ResponseEntity<>(page.getContent().stream()
                .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * PUT /templates -> create new template
     *
     * <p>
     * Creates new Template.
     * For correct saving  only <b>name</b> and <b>content(optional)</b> params should be specified
     * in the received template DTO.
     * Other parameters will be auto-generated
     * </p>
     *
     * @param templateDTO template for save
     * @return saved template item wrapped to ResponseEntity
     */
    @RequestMapping(value = "/templates",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentTemplateDTO> createTemplate(@Valid @RequestBody ExperimentTemplateDTO templateDTO)
            throws URISyntaxException {

        if (templateDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("template", "idexists",
                    "A new template cannot already have an ID")).body(null);
        }
        ExperimentTemplateDTO result = templateService.createTemplate(templateDTO);
        return ResponseEntity.created(new URI("/api/templates/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("template", result.getId()))
                .body(result);
    }


    /**
     * PUT /templates/:id -> create new template
     *
     * <p>
     * Edit existing Template.
     * Method enabled for users with "ADMIN" authorities.
     * For correct saving  only <b>name</b>, <b>content(optional)</b> and <b>id</b> params should be specified
     * in the received template DTO.
     * Other parameters will be auto-generated.
     * Template id should corresponds to existing template item.
     * </p>
     *
     * @param template template for save
     * @return saved template item wrapped to ResponseEntity
     */
    @RequestMapping(value = "/templates",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentTemplateDTO> updateTemplate(@RequestBody ExperimentTemplateDTO template){
        if(!templateService.getTemplateById(template.getId()).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("template", template.getId()))
                .body(templateService.updateTemplate(template));
    }

    /**
     * DELETE /templates/:id -> delete template
     *
     * <p>
     * Delete Template.
     * Method enabled for users with "ADMIN" authorities.
     * Template id should corresponds to existing template item.
     * Template will not be deleted if any Experiments assigned on it
     * </p>
     *
     * @param id id of template
     * @return operation status Response Entity
     */
    @RequestMapping(value = "/templates/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        //do not delete template if  experiments assigned
        if(experimentRepository.countByTemplateId(id) > 0){
            String message = String.format(WARNING_EXPERIMENTS_ASSIGNED, id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers( HeaderUtil.createAlert(message, id)).
                    build();
        }
        templateService.deleteTemplate(id);
        return ResponseEntity.ok().headers(
                HeaderUtil.createEntityDeletionAlert("template", id)).build();
    }


    /**
     * GET /templates/components/:id -> get template by id
     */
    @RequestMapping(value = "/templates/components/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComponentTemplateDTO> getComponentTemplate(@PathVariable String id) {
        return templateService.getComponentTemplateById(id)
                .map(component -> new ResponseEntity<>(component, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /templates/components -> fetch all component template list
     */
    @RequestMapping(value = "/templates/components",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ComponentTemplateDTO>> getAllComponentTemplates(Pageable pageable)
            throws URISyntaxException {
        Page<ComponentTemplateDTO> page = templateService.getAllComponentTemplates(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/templates/components");
        return new ResponseEntity<>(page.getContent().stream()
                .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     *  POST /templates/components -> create new component template
     */
    @RequestMapping(value = "/templates/components",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComponentTemplateDTO> createComponentTemplate(@Valid @RequestBody ComponentTemplateDTO templateDTO)
            throws URISyntaxException {

        if (templateDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("component", "idexists",
                    "A new component template cannot already have an ID")).body(null);
        }
        ComponentTemplateDTO result = templateService.createComponentTemplate(templateDTO);
        return ResponseEntity.created(new URI("/api/templates/components/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("component", result.getId()))
                .body(result);
    }

    /**
     *  PUT /templates/components -> update component template
     */
    @RequestMapping(value = "/templates/components",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComponentTemplateDTO> updateComponentTemplate(@RequestBody ComponentTemplateDTO componentTemplate){
        if(!templateService.getComponentTemplateById(componentTemplate.getId()).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("componentTemplate", componentTemplate.getId()))
                .body(templateService.updateComponentTemplate(componentTemplate));
    }


    /**
     *  DELETE /templates/components/:id -> delete component template by id
     */
    @RequestMapping(value = "/templates/components/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteComponentTemplate(@PathVariable String id) {
        //do not delete component template if  it is assigned with any experiment template
        if(experimentTemplateRepository.countByComponentId(id) > 0){
            String message = String.format(WARNING_TEMPLATES_ASSIGNED, id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers( HeaderUtil.createAlert(message, id)).
                    build();
        }
        templateService.deleteComponentTemplate(id);
        return ResponseEntity.ok().headers(
                HeaderUtil.createEntityDeletionAlert("componentTemplate", id)).build();
    }

}
