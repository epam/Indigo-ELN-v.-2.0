package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api
@RestController
@RequestMapping("/api/registration")
public class RegistrationResource {

    @Autowired
    private RegistrationService registrationService;

    @ApiOperation(value = "Gets registration repositories info.", produces = "application/json")
    @RequestMapping(value = "/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegistrationRepositoryInfo>> info() {
        return ResponseEntity.ok(registrationService.getRepositoriesInfo());
    }

    @ApiOperation(value = "Registers batches.", produces = "application/json")
    @RequestMapping(value = "/register", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Long register(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Batch numbers") List<String> fullBatchNumbers
        ) throws RegistrationException {
        return registrationService.register(id, fullBatchNumbers);
    }

    @ApiOperation(value = "Returns registration status.", produces = "application/json")
    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationStatus status(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Registration job id") Long jobId
        ) throws RegistrationException {
        return registrationService.getStatus(id, jobId);
    }

    @ApiOperation(value = "Returns registration compounds.", produces = "application/json")
    @RequestMapping(value = "/compounds", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Compound> compounds(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Registration job id") Long jobId
        ) throws RegistrationException {
        return registrationService.getRegisteredCompounds(id, jobId);
    }

    @ApiOperation(value = "Searches for compounds by substructure.", produces = "application/json")
    @RequestMapping(value = "/search/substructure", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSubstructure(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Substructure") String structure,
            @ApiParam("Search options") String searchOption
        ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSubstructure(id, structure, searchOption));
    }

    @ApiOperation(value = "Searches for compounds by similarity.", produces = "application/json")
    @RequestMapping(value = "/search/similarity", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSimilarity(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure,
            @ApiParam("Search options") String searchOption
        ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSimilarity(id, structure, searchOption));
    }

    @ApiOperation(value = "Smarts search.", produces = "application/json")
    @RequestMapping(value = "/search/smarts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSmarts(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure
        ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSmarts(id, structure));
    }

    @ApiOperation(value = "Exact search.", produces = "application/json")
    @RequestMapping(value = "/search/exact", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchExact(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure,
            @ApiParam("Search options") String searchOption
        ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchExact(id, structure, searchOption));
    }

}
