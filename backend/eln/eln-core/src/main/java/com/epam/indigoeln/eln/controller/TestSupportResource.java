package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.TestSupportAPI;
import com.epam.indigoeln.eln.service.TestSupportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path(BaseAPI.BASE_PATH)
public class TestSupportResource implements TestSupportAPI {

    @Inject
    TestSupportService testSupportService;

    @Override
    public void cleanupDatabase() {
        testSupportService.cleanupDatabase();
    }
}
