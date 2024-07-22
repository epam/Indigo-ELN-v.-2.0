/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ManagedUserDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing users.
 * <p>
 * <p>This class accesses the User entity, and needs to fetch its collection of authorities.</p>
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * </p>
 * <p>
 * We use a DTO for 3 reasons:
 * <ul></ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </p>
 * <p>Another option would be to have a specific JPA entity graph to handle this case.</p>
 */
@Api
@RestController
@RequestMapping(UserResource.URL_MAPPING)
public class UserResource {

    static final String URL_MAPPING = "/api/users";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CustomDtoMapper dtoMapper;

    @Value("${password.validation}")
    private String passwordValidationRegex;

    /**
     * TODO Think about using UserDTO for all users, but ManagedUserDTO only for USER_EDITOR
     * GET  /users -> Returns all users<br/>.
     * Also use a <b>pageable</b> interface: <b>page</b>, <b>size</b>, <b>sort</b><br/>
     * <b>Example</b>: page=0&size=30&sort=firstname&sort=lastname,asc - retrieves all elements in specified order
     * (<b>firstname</b>: ASC, <b>lastname</b>: ASC) from 0 page with size equals to 30<br/>
     * <b>By default</b>: page = 0, size = 20 and no sort<br/>
     * <b>Available sort options</b>: login, firstName, lastName, email, activated
     *
     * @param pageable Pagination information
     * @return List with users
     * @throws URISyntaxException If URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns all users (with paging).")
    public ResponseEntity<List<ManagedUserDTO>> getAllUsers(
            @ApiParam("Paging data.") Pageable pageable
    ) throws URISyntaxException {
        LOGGER.debug("REST request to get all users");
        Page<User> page = userService.getAllUsers(pageable);

        List<ManagedUserDTO> managedUserDTOs = page.getContent().stream()
                .map(ManagedUserDTO::new).collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return ResponseEntity.ok().headers(headers).body(managedUserDTOs);
    }

    /**
     * GET  api/users/permission-management -> Returns users for Entity Permission Management
     * Don't use it for Authority-management operations!
     *
     * @param pageable Pagination information
     * @return List with users
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Returns users for Entity Permission Management (with paging).")
    @RequestMapping(value = "/permission-management", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getAllUsersWithAuthorities(
            @ApiParam("Paging data.") Pageable pageable
    ) throws URISyntaxException {
        LOGGER.debug("REST request to get all users for permission management");
        Page<User> page = userService.getAllUsers(pageable);

        List<UserDTO> userDTOs = page.getContent().stream()
                .map(UserDTO::new).collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return ResponseEntity.ok().headers(headers).body(userDTOs);
    }

    /**
     * TODO Think about using UserDTO for all users, but ManagedUserDTO only for USER_EDITOR
     * GET  /users/:login -> Returns specified user.
     *
     * @param login Login
     * @return User
     */
    @ApiOperation(value = "Returns user by it's login.")
    @RequestMapping(value = "/{login:[_'.@a-z0-9-]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ManagedUserDTO> getUser(
            @ApiParam("User login") @PathVariable String login
    ) {
        LOGGER.debug("REST request to get user : {}", login);
        User user = userService.getUserWithAuthoritiesByLogin(login);
        return ResponseEntity.ok(new ManagedUserDTO(user));
    }

    /**
     * GET  /users?search=loginOrFirstNameOrLastNameOrSystemRoleName ->
     * returns users with login firstName of lastName like query param
     * {@code loginOrFirstNameOrLastNameOrSystemRoleName}.
     *
     * @param pageable                                   Pagination information
     * @param loginOrFirstNameOrLastNameOrSystemRoleName string to find like in login firstName of lastName
     * @return users with login firstName of lastName like query param {@code search}
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Returns users with login or firstName or lastName like in request param.")
    @RequestMapping(method = RequestMethod.GET,
            params = {"search"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ManagedUserDTO>> getUserByLoginOrFirstNameOrLastName(
            @ApiParam("Paging data.") Pageable pageable,
            @ApiParam("User login or firstName or lastName")
            @RequestParam("search") String loginOrFirstNameOrLastNameOrSystemRoleName
    ) throws URISyntaxException {
        LOGGER.debug("REST request to search users");
        Page<User> page = userService.searchUserByLoginOrFirstNameOrLastNameOrSystemRoleNameWithPaging(
                loginOrFirstNameOrLastNameOrSystemRoleName,
                pageable);

        List<ManagedUserDTO> managedUserDTOs = page.getContent().stream()
                .map(ManagedUserDTO::new).collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return ResponseEntity.ok().headers(headers).body(managedUserDTOs);
    }

    /**
     * POST  /users -> Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     * </p>
     *
     * @param managedUserDTO User
     * @return User
     * @throws URISyntaxException If URI is not correct
     */
    @ApiOperation(value = "Creates user.")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ManagedUserDTO> createUser(
            @ApiParam("User to create") @RequestBody ManagedUserDTO managedUserDTO
    ) throws URISyntaxException {
        LOGGER.debug("REST request to create user: {}", managedUserDTO);
        User user = dtoMapper.convertFromDTO(managedUserDTO);
        user = userService.createUser(user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert("User", user.getLogin());
        return ResponseEntity.created(new URI(URL_MAPPING + "/" + user.getLogin()))
                .headers(headers).body(new ManagedUserDTO(user));
    }

    /**
     * PUT  /users -> Updates an existing User.
     *
     * @param managedUserDTO User
     * @return User
     */
    @ApiOperation(value = "Updates user.")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ManagedUserDTO> updateUser(
            @ApiParam("User to update") @RequestBody ManagedUserDTO managedUserDTO
    ) {
        LOGGER.debug("REST request to update user: {}", managedUserDTO);
        User currentUser = userService.getUserWithAuthorities();
        User user = dtoMapper.convertFromDTO(managedUserDTO);
        user = userService.updateUser(user, currentUser);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert("User", user.getLogin());
        return ResponseEntity.ok().headers(headers).body(new ManagedUserDTO(user));
    }

    /**
     * DELETE  USER :login -> delete the "login" User.
     *
     * @param login Login
     */
    @ApiOperation(value = "Removes user.")
    @RequestMapping(value = "/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(
            @ApiParam("User login to delete") @PathVariable String login
    ) {
        LOGGER.debug("REST request to delete user: {}", login);
        User currentUser = userService.getUserWithAuthorities();
        userService.deleteUserByLogin(login, currentUser);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert("User", login);
        return ResponseEntity.ok().headers(headers).build();
    }


    /**
     * GET /api/users/passwordValidationRegex -> Returns regex for users' password validation.
     *
     * @return password validation regex
     */
    @ApiOperation(value = "Returns password validation regex.")
    @RequestMapping(value = "/passwordValidationRegex", method = RequestMethod.GET)
    public String getUserPasswordValidationRegex() {
        return passwordValidationRegex;
    }

    /**
     * GET /api/users/new -> Checks if user login is new or not
     *
     * @param login user login to check
     * @return Map with only one key where value is true or false
     */
    @ApiOperation(value = "Checks if user login is new or not")
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Boolean>> isNew(@ApiParam("user name to check") @RequestParam String login) {
        boolean isNew = userService.isNew(login);
        return ResponseEntity.ok(Collections.singletonMap("isNew", isNew));
    }
}
