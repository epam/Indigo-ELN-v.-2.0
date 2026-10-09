package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.mapper.SRSMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationClient;
import com.epam.indigoeln.sampleregistration.model.SRSCompoundDTO;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;
import static com.google.common.base.Preconditions.checkNotNull;

@ApplicationScoped
class SampleRegistrationServiceSearchProvider implements CatalogSearchProvider {

    @Inject
    @RestClient
    SampleRegistrationClient sampleRegistrationClient;
    @Inject
    CompoundRepository compoundRepository;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    SRSMapper mapper;

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.SRS;
    }

    @Override
    public Page<SampleDTO> search(FindSamplesRequest request, Paging paging) {
        SRSFindSamplesRequest srsRequest = mapper.requestToSRS(request);
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(srsRequest, paging);
        return Page.of(paging, page.getTotalItems(), map(page.getItems(), mapper::sampleFromSRS), page.isHasMore());
    }

    @Override
    public CompoundEntity importCompound(SampleDTO sample) {
        SRSCompoundDTO srsCompound = sampleRegistrationClient.getCompound(checkNotNull(sample.getCompoundID()));
        return compoundService.findOrCreate(srsCompound.getMolFile()
                , dictionaryService.byId(srsCompound.getStereoisomerCode())
                , dictionaryService.byId(srsCompound.getSaltCode())
                , srsCompound.getSaltEQ()
                , SampleSource.SRS, sample.getCompoundKey()
                , sample.getChemicalName()
        );
    }

    @Override
    public byte[] getCompoundPicture(SampleSource source, UUID compoundID) {
        if (source != SampleSource.SRS) {
            return CatalogSearchProvider.super.getCompoundPicture(source, compoundID);
        }
        return sampleRegistrationClient.getCompoundPicture(compoundID);
    }
}
