package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.search.EntitySearchService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.search.SearchServiceFacade;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.search.EntitiesIdsDTO;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * REST Controller for Custom Search Implementation.
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

    @Autowired
    private ExperimentService experimentService;

    /**
     * GET /catalogue -> returns a list of search catalogues.
     *
     * @return Returns a list of search catalogues
     */
    @ApiOperation(value = "Returns a list of search catalogues.")
    @RequestMapping(
            value = "/catalogue",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SearchServiceAPI.Info>> getCatalogues() {
        Collection<SearchServiceAPI.Info> catalogues = searchService.getCatalogues();
        return ResponseEntity.ok(catalogues);
    }

    /**
     * POST /batch -> find batch Components by specified criteria.
     *
     * @param searchRequest Search request
     * @return Batch components
     */
    @ApiOperation(value = "Searches for batch components by specified criteria.")
    @RequestMapping(
            value = "/batch",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Callable<ResponseEntity<Collection<ProductBatchDetailsDTO>>> searchBatches(
            @ApiParam("Search params.") @RequestBody BatchSearchRequest searchRequest) {
        return () -> {
            Collection<ProductBatchDetailsDTO> batchDetails = searchService.findBatches(searchRequest);
            return ResponseEntity.ok(batchDetails);
        };
    }

    /**
     * POST / -> find entities by specified criteria.
     *
     * @param searchRequest Search requesy
     * @return List with search result
     */
    @ApiOperation(value = "Searches for entities by specified criteria.")
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

    /**
     * POST /experiments -> Searches experiments by full name.
     *
     * @param query experiment full name for search
     * @param pageable page ,size, sort settings
     * @return ids list of project, notebook, experiment and experiment full name
     */
    @ApiOperation(value = "Searches experiments by full name")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                    value = "Results page you want to retrieve"),
            @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                    value = "Number of records per page"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(asc|desc). " +
                            "Default sort order is ascending. " +
                            "Multiple sort criteria are supported.")
    })
    @RequestMapping(value = "experiments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntitiesIdsDTO>> findExperiment(
            @ApiParam("Experiment name for search") @RequestParam String query,
            @ApiIgnore Pageable pageable) {
        final User user = userService.getUserWithAuthorities();
        List<EntitiesIdsDTO> entitiesIdsDTOS = experimentService.findExperimentsByFullName(
                user,
                query,
                pageable);
        return ResponseEntity.ok(entitiesIdsDTOS);
    }
}
