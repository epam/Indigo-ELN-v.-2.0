package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.service.exception.AlreadyInUseException;
import com.epam.indigoeln.core.service.role.RoleService;
import com.epam.indigoeln.web.rest.dto.RoleDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(RoleResource.URL_MAPPING)
public class RoleResource {

    static final String URL_MAPPING = "/api/roles";
    private static final String ENTITY_NAME = "Role";

    private final Logger log = LoggerFactory.getLogger(RoleResource.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    CustomDtoMapper dtoMapper;

    /**
     * GET  /roles -> Returns all roles
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RoleDTO>> getAllRoles() throws URISyntaxException {
        log.debug("REST request to get all roles");
        Collection<Role> roles = roleService.getAllRoles();
        List<RoleDTO> result = new ArrayList<>(roles.size());
        result.addAll(roles.stream().map(
                role -> dtoMapper.convertToDTO(role)).collect(Collectors.toList())
        );
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /roles/:id -> Returns specified role.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(@PathVariable("id") String id) {
        log.debug("REST request to get role : {}", id);
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(dtoMapper.convertToDTO(role));
    }

    /**
     * POST  /roles -> Creates a new role.
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> createRole(@RequestBody @Valid RoleDTO roleDTO)
            throws URISyntaxException {
        log.debug("REST request to create role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.createRole(role);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, role.getId());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + role.getId()))
                .headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * PUT  /roles -> Updates an existing role.
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> updateRole(@RequestBody @Valid RoleDTO roleDTO) {
        log.debug("REST request to update role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.updateRole(role);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, role.getId());
        return ResponseEntity.ok().headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * DELETE  /roles/:id -> Removes role with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        log.debug("REST request to delete role: {}", id);
        roleService.deleteRole(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Void> roleAlreadyExists() {
        HttpHeaders headers = HeaderUtil.createErrorAlert("Role name is already in use", ENTITY_NAME);
        return ResponseEntity.badRequest().headers(headers).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> incorrectAuthorities() {
        HttpHeaders headers = HeaderUtil.createFailureAlert("Set of authorities is incorrect", ENTITY_NAME);
        return ResponseEntity.badRequest().headers(headers).build();
    }

    @ExceptionHandler(AlreadyInUseException.class)
    public ResponseEntity<Void> roleAlreadyInUse() {
        HttpHeaders headers = HeaderUtil.createErrorAlert("You can't delete this role", ENTITY_NAME);
        return ResponseEntity.badRequest().headers(headers).build();
    }
}