package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.CompoundAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class CompoundResource implements CompoundAPI {

    @Inject
    CompoundService compoundService;

    @Override
    @SneakyThrows
    public void loadCompoundsFromFile(UploadForm form) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(form.getFile().uploadedFile().toFile()))) {
            compoundService.loadCompoundsFromFile(is);
        }
    }

    @Override
    public Response getCompoundPicture(UUID compoundID) {
        return compoundService.getCompoundPicture(compoundID);
    }

    @Override
    public Page<SampleDTO> findSamples(FindSamplesRequest request, Paging paging) {
        return compoundService.findSamples(request, paging);
    }

    @Override
    public SampleDTO markSample(UUID sampleID) {
        return compoundService.markSample(sampleID, true);
    }

    @Override
    public SampleDTO unmarkSample(UUID sampleID) {
        return compoundService.markSample(sampleID, false);
    }
}
