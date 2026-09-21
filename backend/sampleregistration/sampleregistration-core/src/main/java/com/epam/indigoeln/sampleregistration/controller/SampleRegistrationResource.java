package com.epam.indigoeln.sampleregistration.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAPI;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import com.epam.indigoeln.sampleregistration.service.SampleRegistrationService;
import com.epam.indigoeln.sampleregistration.service.SampleSearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path(SampleRegistrationAPI.BASE_PATH)
public class SampleRegistrationResource implements SampleRegistrationAPI {

    @Inject
    SampleRegistrationService sampleRegistrationService;
    @Inject
    SampleSearchService sampleSearchService;

    @Override
    public SampleRegistrationResponse registerSample(SampleRegistrationRequest request) {
        return sampleRegistrationService.registerSample(request);
    }

    @Override
    public Page<SRSSampleDTO> find(SRSFindSamplesRequest request, int pageNo, int pageSize) {
        return sampleSearchService.find(request, pageNo, pageSize);
    }
}
