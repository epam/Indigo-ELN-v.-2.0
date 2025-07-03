package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
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
public class MiscResource implements MiscAPI {

    @Inject
    ProjectService projectService;
    @Inject
    SupportService supportService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    CompoundService compoundService;

    @Override
    public @NotNull @Valid TotalCounts getTotalCounts() {
        return projectService.getTotalCounts();
    }

    @Override
    public List<Dictionary> getDictionaries() {
        return dictionaryService.getDictionaries();
    }

    @Override
    public List<DictionaryRef> getDictionary(Dictionary dictionary) {
        return dictionaryService.getDictionary(dictionary);
    }

    @Override
    public List<DictionaryDTO> getDictionaryFull(@NotNull Dictionary dictionary) {
        return dictionaryService.getDictionaryFull(dictionary);
    }

    @Override
    public List<DictionaryDTO> updateDictionary(@NotNull Dictionary dictionary, @NotNull @Valid List<DictionaryRequest> content) {
        return dictionaryService.updateDictionary(dictionary, content);
    }

    @Override
    public List<DictionaryRef> getSaltCodes() {
        return dictionaryService.getSaltCodes();
    }

    @Override
    public Map<String, String> migrate() {
        return supportService.migrate();
    }

    @Override
    public void cleanupDatabase() {
        supportService.cleanupDatabase();
    }

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
