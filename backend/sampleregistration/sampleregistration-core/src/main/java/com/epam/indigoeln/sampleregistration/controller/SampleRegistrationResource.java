package com.epam.indigoeln.sampleregistration.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAPI;
import com.epam.indigoeln.sampleregistration.model.SRSCompoundDTO;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import com.epam.indigoeln.sampleregistration.service.SampleRegistrationService;
import com.epam.indigoeln.sampleregistration.service.SampleSearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

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
    public Page<SRSSampleDTO> find(SRSFindSamplesRequest request, Paging paging) {
        return sampleSearchService.find(request, paging);
    }

    @Override
    public SRSCompoundDTO getCompound(UUID id) {
        return sampleSearchService.getCompound(id);
    }

    @Override
    public int loadCompoundsFromFile(UploadForm form) {
        return sampleRegistrationService.loadCompoundsFromFile(form.getUpload().filePath());
    }
}
