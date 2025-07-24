package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.TotalCounts;
import com.epam.indigoeln.eln.service.*;
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
public class MiscResource implements MiscAPI {

    @Inject
    ProjectService projectService;
    @Inject
    SupportService supportService;

    @Override
    public @NotNull @Valid TotalCounts getTotalCounts() {
        return projectService.getTotalCounts();
    }

    @Override
    public Map<String, String> migrate() {
        return supportService.migrate();
    }
}
