package com.epam.indigoeln.web.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.epam.indigoeln.core.service.search.SearchServiceConstants;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epam.indigoeln.core.service.search.SearchServiceAPI;

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
    public ResponseEntity<Collection<ProductBatchDetailsDTO>> searchByMolecularStructure(
            @RequestBody String structure,
            @RequestParam(name = "searchMode", required = false) Optional<String> searchMode,
            @RequestParam(name = "similarity", required = false) Optional<Float> similarity) {
        String searchModeWithDef = searchMode.orElse(SearchServiceConstants.CHEMISTRY_SEARCH_EXACT);
        Map searchOptions = ImmutableMap.of("min", similarity.orElse(0f));

        Collection<ProductBatchDetailsDTO> batchDetails =
                searchService.searchByMolecularStructure(structure, searchModeWithDef, searchOptions);
        return ResponseEntity.ok(batchDetails);
    }

    /**
     * GET /batches/{number} -> find batch Component details by full batch number
     */
    @RequestMapping(
            value = "/batches/number/{fullBatchNumber}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductBatchDetailsDTO> searchByNotebookBatchNumber(@PathVariable String fullBatchNumber)  {
        return searchService.searchByNotebookBatchNumber(fullBatchNumber)
                 .map(batchDetails -> new ResponseEntity<>(batchDetails, HttpStatus.OK))
                 .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
