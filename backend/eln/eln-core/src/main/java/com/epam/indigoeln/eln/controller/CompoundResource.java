package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.compound.service.search.SampleSearchService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.CompoundAPI;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class CompoundResource implements CompoundAPI {

    @Inject
    CompoundService compoundService;
    @Inject
    SampleSearchService sampleSearchService;

    @Override
    @SneakyThrows
    public void loadCompoundsFromFile(UploadForm form) {
        compoundService.loadCompoundsFromFile(form.getFile().filePath(), true);
    }

    @Override
    public byte[] getCompoundPicture(UUID compoundID) {
        return compoundService.getCompoundPicture(compoundID);
    }

    @Override
    public byte[] getExternalPicture(String inchi) {
        return compoundService.getExternalPicture(inchi);
    }

    @Override
    public SampleSearchResult search(FindSamplesRequest request, @Nullable Integer pageSize) {
        return sampleSearchService.search(request, pageSize);
    }

    @Override
    public SampleDTO importFromSearch(SampleDTO searchItem) {
        return sampleSearchService.importSample(searchItem);
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
