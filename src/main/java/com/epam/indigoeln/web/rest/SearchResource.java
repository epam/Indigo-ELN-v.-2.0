package com.epam.indigoeln.web.rest;

import java.util.Collection;
import java.util.Collections;

import com.epam.indigoeln.core.service.search.SearchServiceConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;

/**
 * REST Controller for Custom Search Implementation
 */
@RestController
@RequestMapping("/api/search")
public class SearchResource {

    @Autowired
    @Qualifier("customSearchService")
    private SearchServiceAPI searchService;

    /**
     * POST /batches/structure -> find batch Components by molecular structure
     */
    @RequestMapping(
            value = "/batches/structure",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ComponentDTO>> searchByMolecularStructure(@RequestBody String structure) {
        Collection<ComponentDTO> componentInfoByBatchNumber =
                searchService.searchByMolecularStructure(structure, SearchServiceConstants.CHEMISTRY_SEARCH_EXACT, Collections.emptyMap());
        return ResponseEntity.ok(componentInfoByBatchNumber);
    }

    /**
     * GET /batches/{number} -> find batch Component details by full batch number
     */
    @RequestMapping(
            value = "/batches/{fullBatchNumber}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComponentDTO> getComponentInfoByBatchNumber(@PathVariable String fullBatchNumber)  {
        return searchService.getComponentInfoByBatchNumber(fullBatchNumber)
                 .map(component -> new ResponseEntity<>(component, HttpStatus.OK))
                 .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
