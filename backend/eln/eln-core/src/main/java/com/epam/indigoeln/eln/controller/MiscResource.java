package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.service.SupportService;
import com.epan.indigoeln.flyway.service.DatabaseInitializationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.Map;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class MiscResource implements MiscAPI {

    @Inject
    ProjectService projectService;
    @Inject
    DatabaseInitializationService databaseInitializationService;
    @Inject
    SupportService supportService;

    @Override
    public @NotNull @Valid TotalCounts getTotalCounts() {
        return projectService.getTotalCounts();
    }

    @Override
    public Map<String, String> migrate() {
        return databaseInitializationService.migrate();
    }

    @Override
    public Map<String, String> insertTestData(UUID templateID) {
        return supportService.insertTestData(templateID);
    }
}
