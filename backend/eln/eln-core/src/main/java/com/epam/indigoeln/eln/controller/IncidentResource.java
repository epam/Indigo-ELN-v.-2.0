package com.epam.indigoeln.eln.controller;

import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.IncidentAPI;
import com.epam.indigoeln.eln.api.IncidentReportForm;
import com.epam.indigoeln.eln.service.incident.IncidentReportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path(BaseAPI.BASE_PATH)
public class IncidentResource implements IncidentAPI {

    @Inject
    IncidentReportService incidentReportService;

    @Override
    public void createIncidentReport(@Valid IncidentReportForm form) {
        incidentReportService.createIncidentReport(form);
    }
}
