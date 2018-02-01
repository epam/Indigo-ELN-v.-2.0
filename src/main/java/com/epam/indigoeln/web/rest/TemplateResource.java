package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.template.TemplateService;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Resources.
 */
@Api
@RestController
@RequestMapping("/api/templates")
public class TemplateResource {

    private static final String TEMPLATE = "template";
    @Autowired
    private TemplateService templateService;

    /**
     * GET /templates/:id -> get template by id.
     *
     * @param id Template's identifier
     * @return Template
     */
    @ApiOperation(value = "Returns template by it's id.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateDTO> getTemplate(
            @ApiParam("Template id") @PathVariable String id
    ) {
        return templateService.getTemplateById(id)
                .map(template -> new ResponseEntity<>(template, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /templates -> fetch all template list.
     *
     * @param pageable Pagination information
     * @return List with templates
     * @throws URISyntaxException if URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns all templates (with paging).")
    public ResponseEntity<List<TemplateDTO>> getAllTemplates(
            @ApiParam("Paging data") Pageable pageable
    ) throws URISyntaxException {
        Page<TemplateDTO> page = templateService.getAllTemplates(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/templates");
        return new ResponseEntity<>(new LinkedList<>(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET /templates?search=nameLike -> fetch template list with likely name.
     *
     * @param pageable Pagination information
     * @param nameLike Name to search liked
     * @return List with templates
     * @throws URISyntaxException if URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            params = "search",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns templates with likely name(with paging).")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByNameLike(
            @ApiParam("Paging data") Pageable pageable,
            @ApiParam("Name to search") @RequestParam("search") String nameLike
    ) throws URISyntaxException {
        Page<TemplateDTO> page = templateService.getTemplatesNameLike(nameLike, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/templates");
        return new ResponseEntity<>(new LinkedList<>(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * PUT /templates -> create new template.
     * <p>
     * <p>
     * Creates new Template.
     * For correct saving  only <b>name</b> and <b>content(optional)</b> params should be specified
     * in the received template DTO.
     * Other parameters will be auto-generated
     * </p>
     *
     * @param templateDTO template for save
     * @return saved template item wrapped to ResponseEntity
     * @throws URISyntaxException if URI is not correct
     */
    @ApiOperation(value = "Creates new template.")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createTemplate(
            @ApiParam("Template to create") @Valid @RequestBody TemplateDTO templateDTO
    ) throws URISyntaxException {
        TemplateDTO result = templateService.createTemplate(templateDTO);
        return ResponseEntity.created(new URI("/api/templates/" + result.getId()))
                .headers(HeaderUtil.createEntityCreateAlert(TEMPLATE, result.getName()))
                .body(result);
    }


    /**
     * PUT /templates/:id -> create new template.
     * <p>
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
    @ApiOperation(value = "Updates existing template.")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateDTO> updateTemplate(
            @ApiParam("Template to update") @RequestBody TemplateDTO template
    ) {
        if (!templateService.getTemplateById(template.getId()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(TEMPLATE, template.getName()))
                .body(templateService.updateTemplate(template));
    }

    /**
     * DELETE /templates/:id -> delete template.
     * <p>
     * <p>
     * Delete Template.
     * Template id should corresponds to existing template item.
     * Template will not be deleted if any Experiments assigned on it
     * </p>
     *
     * @param id id of template
     * @return operation status Response Entity
     */
    @ApiOperation(value = "Removes template.")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTemplate(
            @ApiParam("Template id to delete") @PathVariable String id
    ) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok().headers(
                HeaderUtil.createEntityDeleteAlert(TEMPLATE, null)).build();
    }


    /**
     * GET /templates/new -> Checks if template's name already exists
     *
     * @param templateName Template's name to check
     * @return Map with only one key where value is true or false
     */
    @ApiOperation(value = "Checks if template's name is new or not")
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> isNew(@ApiParam("Template's name to check")
                                                      @RequestParam String templateName) {
        boolean notNew = templateService.nameAlreadyExists(templateName);
        return ResponseEntity.ok(Collections.singletonMap("nameAlreadyExists", notNew));
    }
}
