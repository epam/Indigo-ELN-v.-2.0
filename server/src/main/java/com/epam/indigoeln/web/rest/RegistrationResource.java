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

import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.model.CompoundTableRowInfo;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/registration")
public class RegistrationResource {

    @Autowired
    private RegistrationService registrationService;

    @ApiOperation(value = "Gets registration repositories info.")
    @RequestMapping(value = "/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegistrationRepositoryInfo>> info() {
        return ResponseEntity.ok(registrationService.getRepositoriesInfo());
    }

    @ApiOperation(value = "Registers batches.")
    @RequestMapping(value = "/{repositoryId}/register", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String register(
            @ApiParam("Registration repository id") @PathVariable("repositoryId") String id,
            @ApiParam("Batch numbers") @RequestBody String[] fullBatchNumbers
    ) throws RegistrationException {
        return registrationService.register(id, Arrays.asList(fullBatchNumbers));
    }

    @ApiOperation(value = "Registers batches.")
    @RequestMapping(value = "/register", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String register(@ApiParam("Batch numbers") @RequestBody String[] fullBatchNumbers
    ) throws RegistrationException {
        return registrationService.register(getRepositoryId(), Arrays.asList(fullBatchNumbers));
    }

    @ApiOperation(value = "Returns registration status.")
    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationStatus status(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Registration job id") String jobId
    ) throws RegistrationException {
        return registrationService.getStatus(StringUtils.isBlank(id) ? getRepositoryId() : id, jobId);
    }

    @ApiOperation(value = "Returns registration compounds.")
    @RequestMapping(value = "/compounds", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Compound> compoundsByJobId(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Registration job id") String jobId
    ) throws RegistrationException {
        return registrationService.getRegisteredCompounds(StringUtils.isBlank(id) ? getRepositoryId() : id, jobId);
    }

    @ApiOperation(value = "Returns compounds by their number.")
    @RequestMapping(value = "/compounds/{compoundNo}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompoundTableRowInfo> compoundsByCompoundNo(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Compound id") @PathVariable("compoundNo") String compoundNo
    ) throws RegistrationException {
        return registrationService.getCompoundInfoByCompoundNo(StringUtils.isBlank(id)
                ? getRepositoryId() : id, compoundNo);
    }

    @ApiOperation(value = "Searches for compounds by substructure.")
    @RequestMapping(value = "/search/substructure", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSubstructure(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Substructure") String structure,
            @ApiParam("Search options") String searchOption
    ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSubstructure(StringUtils.isBlank(id)
                ? getRepositoryId() : id, structure, searchOption));
    }

    @ApiOperation(value = "Searches for compounds by similarity.")
    @RequestMapping(value = "/search/similarity", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSimilarity(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure,
            @ApiParam("Search options") String searchOption
    ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSimilarity(StringUtils.isBlank(id)
                ? getRepositoryId() : id, structure, searchOption));
    }

    @ApiOperation(value = "Smarts search.")
    @RequestMapping(value = "/search/smarts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSmarts(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure
    ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSmarts(StringUtils.isBlank(id)
                ? getRepositoryId() : id, structure));
    }

    @ApiOperation(value = "Exact search.")
    @RequestMapping(value = "/search/exact", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchExact(
            @ApiParam("Registration repository id") String id,
            @ApiParam("Structure") String structure,
            @ApiParam("Search options") String searchOption
    ) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchExact(StringUtils.isBlank(id)
                ? getRepositoryId() : id, structure, searchOption));
    }

    private String getRepositoryId() throws RegistrationException {
        final List<RegistrationRepositoryInfo> repositoriesInfo = registrationService.getRepositoriesInfo();
        if (repositoriesInfo != null && repositoriesInfo.size() == 1) {
            return repositoriesInfo.get(0).getId();
        } else {
            throw new RegistrationException("More than one registration repository found, "
                    + "please specify repository id.");
        }
    }
}
