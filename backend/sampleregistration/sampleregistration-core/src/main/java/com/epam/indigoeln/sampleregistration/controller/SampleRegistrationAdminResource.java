package com.epam.indigoeln.sampleregistration.controller;


import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAPI;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAdminAPI;
import com.epam.indigoeln.sampleregistration.service.SampleRegistrationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

import java.util.Map;

@Slf4j
@Path(SampleRegistrationAPI.BASE_PATH)
public class SampleRegistrationAdminResource implements SampleRegistrationAdminAPI {

    @Inject
    Flyway flyway;
    @Inject
    SampleRegistrationService sampleRegistrationService;

    @Override
    public void migrate() {
        flyway.migrate();
    }

    @Override
    public Map<String, String> reindexSearchVectors() {
        return Map.of("samples", String.valueOf(sampleRegistrationService.reindexSearchVectors()));
    }
}
