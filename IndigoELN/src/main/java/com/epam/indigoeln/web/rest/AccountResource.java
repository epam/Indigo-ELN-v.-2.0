package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.RoleDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
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

@RestController
@RequestMapping("/api/accounts")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CustomDtoMapper dtoMapper;

    /**
     * GET  /authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account -> get the current user.
     */
    @RequestMapping(value = "/account", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getAccount() {
        User user = userService.getUserWithAuthorities();
        return ResponseEntity.ok(new UserDTO(user));
    }

    /**
     * GET  /account/roles -> get the current user.
     */
    @RequestMapping(value = "/account/roles", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RoleDTO>> getAccountRoles() {
        User user = userService.getUserWithAuthorities();
        List<RoleDTO> result = new ArrayList<>(user.getRoles().size());
        result.addAll(user.getRoles().stream().map(
                role -> dtoMapper.convertToDTO(role)).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }
}