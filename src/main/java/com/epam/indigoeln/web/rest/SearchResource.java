package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.search.EntitySearchService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.search.SearchServiceFacade;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
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
import java.util.List;

/**
 * REST Controller for Custom Search Implementation
 */
@Api
@RestController
@RequestMapping("/api/search")
public class SearchResource {

    @Autowired
    private SearchServiceFacade searchService;

    @Autowired
    private EntitySearchService entitySearchService;

    @Autowired
    private UserService userService;

    /**
     * GET /catalogue -> returns a list of search catalogues
     */
    @ApiOperation(value = "Returns a list of search catalogues.", produces = "application/json")
    @RequestMapping(
            value = "/catalogue",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SearchServiceAPI.Info>> getCatalogues() {
        Collection<SearchServiceAPI.Info> catalogues = searchService.getCatalogues();
        return ResponseEntity.ok(catalogues);
    }

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

    /**
     * POST / -> find entities by specified criteria
     */
    @ApiOperation(value = "Searches for entities by specified criteria.", produces = "application/json")
    @RequestMapping(
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntitySearchResultDTO>> search(
            @ApiParam("Search params.") @RequestBody EntitySearchRequest searchRequest
    ) {
        final User user = userService.getUserWithAuthorities();
        List<EntitySearchResultDTO> batchDetails = entitySearchService.find(user, searchRequest);
        return ResponseEntity.ok(batchDetails);
    }
}
