package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Role;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

@RestController
@RequestMapping(RoleResource.URL_MAPPING)
public class RoleResource {

    static final String URL_MAPPING = "/api/roles";

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
    public ResponseEntity<Collection<Role>> getAllRoles() throws URISyntaxException {
        log.debug("REST request to get all roles");
        Collection<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
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
    public ResponseEntity<RoleDTO> createUser(@RequestBody @Valid RoleDTO roleDTO)
            throws URISyntaxException {
        log.debug("REST request to create role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.createRole(role);
        HttpHeaders headers = HeaderUtil.createAlert("The role is created", role.getId());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + role.getId()))
                .headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * PUT  /roles -> Updates an existing role.
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> updateUser(@RequestBody @Valid RoleDTO roleDTO) {
        log.debug("REST request to update role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.updateRole(role);
        HttpHeaders headers = HeaderUtil.createAlert("The role is updated", role.getId());
        return ResponseEntity.ok().headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * DELETE  /roles/:id -> Removes role with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.debug("REST request to delete role: {}", id);
        roleService.deleteRole(id);
        HttpHeaders headers = HeaderUtil.createAlert("The role is deleted", id);
        return ResponseEntity.ok().headers(headers).build();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Void> roleAlreadyExists() {
        HttpHeaders headers = HeaderUtil.createFailureAlert("role-management", "Role name is already in use");
        return ResponseEntity.badRequest().headers(headers).build();
    }
}