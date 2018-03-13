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

import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.Word;
import com.epam.indigoeln.core.service.dictionary.DictionaryService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.DictionaryDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDictionaryDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping(DictionaryResource.URL_MAPPING)
public class DictionaryResource {

    static final String URL_MAPPING = "api/dictionaries";
    private static final String ENTITY_NAME = "Dictionary";

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryResource.class);

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private UserService userService;

    /**
     * GET /dictionaries/experiments -> get experiments dictionary.
     *
     * @return Returns experiments dictionary
     */
    @ApiOperation(value = "Returns experiments dictionary.")
    @RequestMapping(value = "/experiments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDictionaryDTO> getExperiments() {
        LOGGER.debug("REST request to get dictionary for experiments");
        User user = userService.getUserWithAuthorities();
        return new ResponseEntity<>(dictionaryService.getExperiments(user), HttpStatus.OK);
    }

    /**
     * GET /dictionaries/users -> get users dictionary.
     *
     * @return Returns users dictionary
     */
    @ApiOperation(value = "Returns users dictionary.")
    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> getUsers() {
        LOGGER.debug("REST request to get dictionary for users");
        List<User> allUsers = userService.getAllUsers();
        Dictionary dictionary = new Dictionary();
        dictionary.setDescription("all users");
        Set<Word> words = allUsers.stream().map(user -> {
            Word word = new Word();
            word.setId(user.getId());
            word.setName(user.getFullName());
            return word;
        }).collect(Collectors.toSet());
        dictionary.setWords(words);
        DictionaryDTO dict = new DictionaryDTO(dictionary);
        return new ResponseEntity<>(dict, HttpStatus.OK);
    }

    /**
     * GET /dictionaries -> get all dictionaries.
     *
     * @return Returns all dictionaries
     */
    @ApiOperation(value = "Returns all dictionaries.")
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DictionaryDTO>> getDictionaries() {
        LOGGER.debug("REST request to get all dictionaries");
        return new ResponseEntity<>(dictionaryService.getAllDictionaries(), HttpStatus.OK);
    }

    /**
     * GET /dictionaries/:id -> get dictionary by id.
     *
     * @param id Identifier of the dictionary
     * @return Returns dictionary by it's id
     */
    @ApiOperation(value = "Returns dictionary by it's id.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> getDictionary(
            @ApiParam("Identifier of the dictionary.") @PathVariable String id
    ) {
        LOGGER.debug("REST request to get dictionary with id: {}", id);
        return dictionaryService.getDictionaryById(id)
                .map(dict -> new ResponseEntity<>(dict, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /dictionaries/byName/:name -> get dictionary by name.
     *
     * @param name Name of the dictionary
     * @return Returns dictionary by it's name
     */
    @ApiOperation(value = "Returns dictionary by it's name.")
    @RequestMapping(value = "/byName/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> getDictionaryByName(
            @ApiParam("Name of the dictionary.") @PathVariable String name
    ) {
        LOGGER.debug("REST request to get dictionary with name: {}", name);
        return dictionaryService.getDictionaryByName(name)
                .map(dict -> new ResponseEntity<>(dict, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /dictionaries -> fetch all dictionary list.
     *
     * @param pageable Paging data
     * @return Returns all found dictionaries (with paging)
     * @throws URISyntaxException If URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns all dictionaries.")
    public ResponseEntity<List<DictionaryDTO>> getAllDictionaries(
            @ApiParam("Paging data. "
                    + "Allows to sort by name of field within query params"
                    + " sort=<fieldName>,<asc|desc>.") Pageable pageable
    ) throws URISyntaxException {
        LOGGER.debug("REST request to get all dictionaries");
        Page<DictionaryDTO> page = dictionaryService.getAllDictionaries("", pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return new ResponseEntity<>(new LinkedList<>(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET /dictionaries?search={@code search} -> fetch all dictionary with name or description like {@code search}.
     *
     * @param pageable Paging data
     * @param search   Search string
     * @return Returns all found dictionaries (with paging)
     * @throws URISyntaxException If URI is not correct
     */
    @RequestMapping(method = RequestMethod.GET,
            params = "search",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns all found dictionaries (with paging).")
    public ResponseEntity<List<DictionaryDTO>> getAllDictionaries(
            @ApiParam("Paging data. Allows to sort by name of field within query params "
                    + "sort=<fieldName>,<asc|desc>.") Pageable pageable,
            @ApiParam("Search string. Allows search dictionaries by name.")
            @RequestParam(value = "search") String search
    ) throws URISyntaxException {
        LOGGER.debug("REST request to to search for dictionaries");
        Page<DictionaryDTO> page = dictionaryService.getAllDictionaries(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return new ResponseEntity<>(new LinkedList<>(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * POST /dictionaries -> create new dictionary.
     *
     * @param dictionaryDTO Dictionary to create
     * @return Created dictionary
     */
    @ApiOperation(value = "Creates new dictionary.")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDictionary(
            @ApiParam("Dictionary to create.") @RequestBody @Valid DictionaryDTO dictionaryDTO
    ) {
        LOGGER.debug("REST request to create new dictionary: {}", dictionaryDTO);
        User user = userService.getUserWithAuthorities();
        DictionaryDTO result = dictionaryService.createDictionary(dictionaryDTO, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, dictionaryDTO.getName());
        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * PUT  /dictionaries -> Updates an existing dictionary.
     *
     * @param dictionaryDTO Dictionary to create
     * @return Updated dictionary
     */
    @ApiOperation(value = "Updates dictionary.")
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> updateRole(
            @ApiParam("Dictionary to create.") @RequestBody @Valid DictionaryDTO dictionaryDTO
    ) {
        LOGGER.debug("REST request to update dictionary: {}", dictionaryDTO);
        DictionaryDTO updatedDictDTO = dictionaryService.updateDictionary(dictionaryDTO);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedDictDTO.getName());
        return ResponseEntity.ok().headers(headers).body(updatedDictDTO);
    }

    /**
     * DELETE  /dictionaries/:id -> Removes dictionary with specified id.
     *
     * @param id Id of the dictionary to delete
     */
    @ApiOperation(value = "Deletes dictionary.")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteDictionary(
            @ApiParam("Id of the dictionary to delete.") @PathVariable String id
    ) {
        LOGGER.debug("REST request to delete dictionary: {}", id);
        dictionaryService.deleteDictionary(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, null);
        return ResponseEntity.ok().headers(headers).build();
    }
}
