package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.compound.service.search.SampleSearchService;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.CompoundAPI;
import com.epam.indigoeln.eln.model.SampleSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class CompoundResource implements CompoundAPI {

    @Inject
    CompoundService compoundService;
    @Inject
    SampleSearchService sampleSearchService;

    @Override
    public byte[] getCompoundPicture(UUID compoundID) {
        return compoundService.getCompoundPicture(compoundID);
    }

    @Override
    public byte[] getCatalogCompoundPicture(SearchCatalog catalog, SampleSource source, UUID compoundID) {
        return sampleSearchService.getCompoundPicture(catalog, source, compoundID);
    }

    @Override
    public byte[] getExternalPicture(String inchi) {
        return compoundService.getExternalPicture(inchi);
    }

    @Override
    public Page<SampleDTO> search(FindSamplesRequest request, Paging paging) {
        return sampleSearchService.search(request, paging);
    }

    @Override
    public SampleDTO markSample(SampleDTO sample) {
        return sampleSearchService.markSample(sample);
    }

    @Override
    public SampleDTO unmarkSample(SampleDTO sample) {
        return sampleSearchService.unmarkSample(sample);
    }
}
