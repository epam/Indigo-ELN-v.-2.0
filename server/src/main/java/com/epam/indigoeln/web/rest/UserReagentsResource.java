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
import com.epam.indigoeln.core.service.userreagents.UserReagentsService;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(UserReagentsResource.URL_MAPPING)
public class UserReagentsResource {

    static final String URL_MAPPING = "/api/user_reagents";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserReagentsResource.class);
    @Autowired
    private CustomDtoMapper dtoMapper;
    @Autowired
    private UserReagentsService userReagentsService;
    @Autowired
    private UserService userService;

    @Operation(summary = "Returns user favourite reagents.")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> getUserReagents() {
        LOGGER.debug("REST request to get all user reagents");
        User currentUser = userService.getUserWithAuthorities();
        final List<Object> reagents = userReagentsService.getUserReagents(currentUser);
        return ResponseEntity.ok(reagents);
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Saves user favourite reagents.")
    public ResponseEntity<Void> saveUserReagents(
            @Parameter(description = "Reagents list.") @RequestBody List<Object> reagents) {
        LOGGER.debug("REST request to save user reagents: {}", reagents);
        User currentUser = userService.getUserWithAuthorities();
        userReagentsService.saveUserReagents(currentUser, reagents);
        return ResponseEntity.ok().build();
    }
}
