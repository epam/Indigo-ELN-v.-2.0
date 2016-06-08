package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.config.ClientConfiguration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping("/api/client_configuration")
public class ClientConfigurationResource {

    @Autowired
    private ClientConfiguration clientConfiguration;

    /**
     * GET  /client_configuration -> Returns client configuration
     */
    @ApiOperation(value = "Returns client configuration.",
            produces = "application/json")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClientConfiguration() {
        return ResponseEntity.ok(clientConfiguration.getProperties());
    }

}
