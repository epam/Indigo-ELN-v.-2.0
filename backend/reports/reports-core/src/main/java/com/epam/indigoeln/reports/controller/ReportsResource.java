package com.epam.indigoeln.reports.controller;


import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.service.ReportsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Path(ReportsAPI.BASE_PATH)
public class ReportsResource implements ReportsAPI {

    @Inject
    ReportsService reportsService;

    @Override
    public Map<String, String> getVersion() {
        return Map.of("service", "reports");
    }

    @Override
    public Response generateExperimentReport(ExperimentReportDataDTO data) {
        return reportsService.generateExperimentReport(data);
    }
}
