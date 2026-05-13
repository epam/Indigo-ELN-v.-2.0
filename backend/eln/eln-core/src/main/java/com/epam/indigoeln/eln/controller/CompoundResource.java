package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.compound.service.search.SampleSearchService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.CompoundAPI;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
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
        try (InputStream is = new BufferedInputStream(new FileInputStream(form.getFile().uploadedFile().toFile()))) {
            compoundService.loadCompoundsFromFile(is);
        }
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
    public SampleSearchResult search(@Valid FindSamplesRequest request, @Nullable @QueryParam("nextCatalog") SearchCatalog nextCatalog, @Nullable @QueryParam("nextAfter") String nextAfter, @Nullable @QueryParam("limit") Integer limit) {
        return sampleSearchService.search(request, nextCatalog, nextAfter, limit);
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
