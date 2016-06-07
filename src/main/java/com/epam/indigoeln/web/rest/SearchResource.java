package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * REST Controller for Custom Search Implementation
 */
@Api
@RestController
@RequestMapping("/api/search")
public class SearchResource {

    @Autowired
    @Qualifier("commonSearchService")
    private SearchServiceAPI searchService;

    /**
     * POST /batch -> find batch Components by specified criteria
     */
    @ApiOperation(value = "Searches for batch components by specified criteria.", produces = "application/json")
    @RequestMapping(
            value = "/batch",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ProductBatchDetailsDTO>> searchBatches(
            @ApiParam("Search params.") @RequestBody BatchSearchRequest searchRequest
        )  {
        Collection<ProductBatchDetailsDTO> batchDetails = searchService.findBatches(searchRequest);
        return ResponseEntity.ok(batchDetails);
    }
}
