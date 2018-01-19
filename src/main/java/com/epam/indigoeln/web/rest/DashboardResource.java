package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.dashboard.DashboardService;
import com.epam.indigoeln.web.rest.dto.DashboardDTO;
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
@RequestMapping(DashboardResource.URL_MAPPING)
public class DashboardResource {

    static final String URL_MAPPING = "/api/dashboard";

    @Autowired
    private DashboardService dashboardService;

    /**
     * GET  /dashboard -> Returns dashboard experiments.
     * 1. Experiments created by current user during one month which are in one of following statuses:
     * 'Open', 'Completed'.
     *
     * @return Returns dashboard content
     */
    @ApiOperation(value = "Returns dashboard content.")
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
