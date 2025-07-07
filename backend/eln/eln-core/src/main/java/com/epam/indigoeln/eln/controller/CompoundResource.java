package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.CompoundAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.service.SupportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
}
