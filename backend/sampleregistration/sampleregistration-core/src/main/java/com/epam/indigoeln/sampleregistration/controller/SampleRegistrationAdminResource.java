package com.epam.indigoeln.sampleregistration.controller;


import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAPI;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAdminAPI;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

@Slf4j
@Path(SampleRegistrationAPI.BASE_PATH)
public class SampleRegistrationAdminResource implements SampleRegistrationAdminAPI {

    @Inject
    Flyway flyway;

    @Override
    public void migrate() {
        flyway.migrate();
    }
}
