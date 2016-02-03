package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.template.TemplateRepository;
import com.epam.indigoeln.core.service.template.TemplateService;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/templates")
public class TemplateResource {

    private final static String WARNING_EXPERIMENTS_ASSIGNED = "Template with identifier %s could not be deleted : any assigned experiments exists.";

    @Autowired
    TemplateService templateService;

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    TemplateRepository templateRepository;

    /**
     * GET /templates/:id -> get template by id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateDTO> getTemplate(@PathVariable String id) {
        return templateService.getTemplateById(id)
                .map(template -> new ResponseEntity<>(template, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * GET /templates -> fetch all template list
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TemplateDTO>> getAllTemplates(Pageable pageable)
            throws URISyntaxException {
        Page<TemplateDTO> page = templateService.getAllTemplates(pageable);
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
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTemplate(@Valid @RequestBody TemplateDTO templateDTO)
            throws URISyntaxException {

        if (templateDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("template",
                    "A new template can't already have an ID")).build();
        }
        TemplateDTO result = templateService.createTemplate(templateDTO);
        return ResponseEntity.created(new URI("/api/templates/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("template", result.getId()))
                .body(result);
    }


    /**
     * PUT /templates/:id -> create new template
     *
     * <p>
     * Edit existing Template.
     * For correct saving  only <b>name</b>, <b>content(optional)</b> and <b>id</b> params should be specified
     * in the received template DTO.
     * Other parameters will be auto-generated.
     * Template id should corresponds to existing template item.
     * </p>
     *
     * @param template template for save
     * @return saved template item wrapped to ResponseEntity
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateDTO> updateTemplate(@RequestBody TemplateDTO template){
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
     * Template id should corresponds to existing template item.
     * Template will not be deleted if any Experiments assigned on it
     * </p>
     *
     * @param id id of template
     * @return operation status Response Entity
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
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
}