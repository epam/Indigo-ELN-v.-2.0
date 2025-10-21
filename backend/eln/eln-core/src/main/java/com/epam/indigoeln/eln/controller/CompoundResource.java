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
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
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
    public List<SampleDTO> findSamples(FindSamplesRequest request) {
        return compoundService.findSamples(request);
    }

    @Override
    public void markSample(UUID sampleID) {
        compoundService.markSample(sampleID, true);
    }

    @Override
    public void unmarkSample(UUID sampleID) {
        compoundService.markSample(sampleID, false);
    }

    @Override
    public Page<SampleDTO> getMarkedSamples(@Nullable String search, Paging paging) {
        return compoundService.listMarkedSamples(search, paging);
    }
}
