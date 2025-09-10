package com.epam.indigoeln.reports.controller;


import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.service.ReportsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path(ReportsAPI.BASE_PATH)
public class ReportsResource implements ReportsAPI {

    @Inject
    ReportsService reportsService;

    @Override
    public Response generateExperimentReport(ExperimentReportDataDTO data) {
        return reportsService.generateExperimentReport(data);
    }

    @Override
    public ProjectDTO test() {
        log.warn("!!! ReportsResource.test() 1");
        ProjectDTO response = new ProjectDTO();
        response.setName("!!!!");
        log.warn("!!! ReportsResource.test() 2");
        return response;
    }
}
