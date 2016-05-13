package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.Word;
import com.epam.indigoeln.core.service.dictionary.DictionaryService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.DictionaryDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

@RestController
@RequestMapping(DictionaryResource.URL_MAPPING)
public class DictionaryResource {

    static final String URL_MAPPING = "api/dictionaries";
    private static final String ENTITY_NAME = "Dictionary";

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryResource.class);

    @Autowired
    CustomDtoMapper dtoMapper;

    @Autowired
    DictionaryService dictionaryService;

    @Autowired
    UserService userService;


    /**
     * GET /dictionaries/users -> get users dictionary
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> getUsers() {
        LOGGER.debug("REST request to get dictionary for users");
        List<User> allUsers = userService.getAllUsers();
        Dictionary dictionary = new Dictionary();
        dictionary.setDescription("all users");
        Set<Word> words = allUsers.stream().map(user -> {
            Word word = new Word();
            word.setName(user.getFullName());
            return word;
        }).collect(Collectors.toSet());
        dictionary.setWords(words);
        DictionaryDTO dict = new DictionaryDTO(dictionary);
        return new ResponseEntity<>(dict, HttpStatus.OK);
    }

    /**
     * GET /dictionaries/:id -> get dictionary by id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> getDictionary(@PathVariable String id) {
        LOGGER.debug("REST request to get dictionary with id: {}", id);
        return dictionaryService.getDictionaryById(id)
                .map(dict -> new ResponseEntity<>(dict, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /dictionaries -> fetch all dictionary list
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DictionaryDTO>> getAllDictionaries(@RequestParam(value = "page") Integer pageno,
                                                                  @RequestParam(value = "size") Integer size,
                                                                  @RequestParam(value = "search") String search) throws URISyntaxException {
        LOGGER.debug("REST request to get all dictionaries");
        Page<DictionaryDTO> page = dictionaryService.getAllDictionaries(new PageRequest(pageno, size), search);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, URL_MAPPING);
        return new ResponseEntity<>(page.getContent().stream()
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * POST /dictionaries -> create new dictionary
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDictionary(@RequestBody @Valid DictionaryDTO dictionaryDTO)
            throws URISyntaxException {
        LOGGER.debug("REST request to create new dictionary: {}", dictionaryDTO);
        DictionaryDTO result = dictionaryService.createDictionary(dictionaryDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT  /dictionaries -> Updates an existing dictionary.
     */
    @RequestMapping(method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DictionaryDTO> updateRole(@RequestBody @Valid DictionaryDTO dictionaryDTO) {
        LOGGER.debug("REST request to update dictionary: {}", dictionaryDTO);
        DictionaryDTO updatedDictDTO = dictionaryService.updateDictionary(dictionaryDTO);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedDictDTO.getId());
        return ResponseEntity.ok().headers(headers).body(updatedDictDTO);
    }
    /**
     * DELETE  /dictionaries/:id -> Removes dictionary with specified id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteDictionary(@PathVariable String id) {
        LOGGER.debug("REST request to delete dictionary: {}", id);
        dictionaryService.deleteDictionary(id);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }



}
