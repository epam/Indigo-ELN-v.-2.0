package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.service.role.RoleService;
import com.epam.indigoeln.web.rest.dto.RoleDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Api
@RestController
@RequestMapping(RoleResource.URL_MAPPING)
public class RoleResource {

    static final String URL_MAPPING = "/api/roles";
    private static final String ENTITY_NAME = "Role";

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleResource.class);
    @Autowired
    private CustomDtoMapper dtoMapper;
    @Autowired
    private RoleService roleService;

    /**
     * GET  /roles -> Returns all roles with pagination.
     *
     * @param pageable Paging data
     * @return Returns all roles
     * @throws URISyntaxException thrown to indicate that a string could not be parsed as a
     *                            URI reference.
     */
    @ApiOperation(value = "Returns all roles.")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RoleDTO>> getAllRoles(@ApiParam("Paging data.") Pageable pageable)
            throws URISyntaxException {
        LOGGER.debug("REST request to get all roles with pagination");
        Page<Role> page = roleService.getAllRoles(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        List<RoleDTO> result = page.getContent().stream()
                .map(dtoMapper::convertToDTO).collect(toList());
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * * GET  /roles?search=nameLike -> Returns roles with name like {@code nameLike}.
     *
     * @param nameLike Searching role name
     * @return Returns with name like {@code nameLike}
     */
    @ApiOperation(value = "Returns roles with name like a string in a parameter.")
    @RequestMapping(method = RequestMethod.GET,
            params = "search",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RoleDTO>> getRolesLike(
            @ApiParam("Searching role name") @RequestParam("search") String nameLike,
            @ApiParam("Paging data.") Pageable pageable
    ) throws URISyntaxException {
        LOGGER.debug("REST request to get roles with liked names");
        Page<Role> page = roleService.getRolesWithNameLike(nameLike, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        List<RoleDTO> result = page.getContent().stream()
                .map(dtoMapper::convertToDTO).collect(toList());
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * GET  /roles/:id -> Returns specified role.
     *
     * @param id Identifier
     * @return Role by id
     */
    @ApiOperation(value = "Returns specified role.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(
            @ApiParam("Role id.") @PathVariable("id") String id
    ) {
        LOGGER.debug("REST request to get role : {}", id);
        Role role = roleService.getRole(id);
        return ResponseEntity.ok(dtoMapper.convertToDTO(role));
    }

    /**
     * POST  /roles -> Creates a new role.
     *
     * @param roleDTO Role
     * @return Created role
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Creates a new role.")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> createRole(
            @ApiParam("Role to create.") @RequestBody @Valid RoleDTO roleDTO
    ) throws URISyntaxException {
        LOGGER.debug("REST request to create role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.createRole(role);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, role.getName());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + role.getId()))
                .headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * PUT  /roles -> Updates an existing role.
     *
     * @param roleDTO Role
     * @return Role
     */
    @ApiOperation(value = "Updates existing role.")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> updateRole(
            @ApiParam("Role to update.") @RequestBody @Valid RoleDTO roleDTO
    ) {
        LOGGER.debug("REST request to update role: {}", roleDTO);
        Role role = dtoMapper.convertFromDTO(roleDTO);
        role = roleService.updateRole(role);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, role.getName());
        return ResponseEntity.ok().headers(headers).body(dtoMapper.convertToDTO(role));
    }

    /**
     * DELETE  /roles/:id -> Removes role with specified id.
     *
     * @param id Id
     */
    @ApiOperation(value = "Deletes role.")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(
            @ApiParam("Role id to delete") @PathVariable String id
    ) {
        LOGGER.debug("REST request to delete role: {}", id);
        roleService.deleteRole(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }
}
