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
import com.epam.indigoeln.web.rest.dto.RoleDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/accounts")
public class AccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CustomDtoMapper dtoMapper;

    /**
     * GET  /authenticate -> check if the user is authenticated, and return its login.
     *
     * @param request Request for authentication
     * @return Returns the login of the user making this request
     */
    @ApiOperation(value = "Checks if user is authenticated and returns it's login.")
    @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
    public String isAuthenticated(HttpServletRequest request) {
        LOGGER.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account -> get the current user.
     *
     * @return Returns current user
     */
    @ApiOperation(value = "Returns current user.")
    @RequestMapping(value = "/account", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getAccount() {
        User user = userService.getUserWithAuthorities();
        return ResponseEntity.ok(new UserDTO(user));
    }

    /**
     * GET  /account/roles -> get the current user.
     *
     * @return Returns current user roles
     */
    @ApiOperation(value = "Returns current user roles.", responseContainer = "List")
    @RequestMapping(value = "/account/roles", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RoleDTO>> getAccountRoles() {
        User user = userService.getUserWithAuthorities();
        List<RoleDTO> result = new ArrayList<>(user.getRoles().size());
        result.addAll(user.getRoles().stream().map(
                role -> dtoMapper.convertToDTO(role)).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Prolongs user's session.")
    @RequestMapping(value = "/prolong", method = RequestMethod.GET)
    public ResponseEntity<Void> prolongSession() {
        return ResponseEntity.ok().build();
    }
}
