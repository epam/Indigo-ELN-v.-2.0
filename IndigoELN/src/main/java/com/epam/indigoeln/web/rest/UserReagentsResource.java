package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.service.userreagents.UserReagentsService;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.mongodb.BasicDBList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserReagentsResource.URL_MAPPING)
public class UserReagentsResource {

    static final String URL_MAPPING = "/api/user_reagents";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserReagentsResource.class);
    @Autowired
    CustomDtoMapper dtoMapper;
    @Autowired
    private UserReagentsService userReagentsService;
    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasicDBList> getUserReagents() {
        LOGGER.debug("REST request to get all user reagents");
        User currentUser = userService.getUserWithAuthorities();
        final BasicDBList reagents = userReagentsService.getUserReagents(currentUser);
        return ResponseEntity.ok(reagents);
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveUserReagents(@RequestBody BasicDBList reagents) {
        LOGGER.debug("REST request to save user reagents: {}", reagents);
        User currentUser = userService.getUserWithAuthorities();
        userReagentsService.saveUserReagents(currentUser, reagents);
        return ResponseEntity.ok().build();
    }

}