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
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static com.epam.indigoeln.common.util.ModelUtil.map;

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
        STRCodeCompound strCode = STRCodeCompound.parse(sample.getCompoundKey());
        SRSCompoundDTO srsCompound = sampleRegistrationClient.getCompound(strCode);
        CompoundEntity compound = compoundService.findOrCreate(srsCompound.getMolFile()
                , srsCompound.getStereoisomerCode() != null ? dictionaryService.byId(srsCompound.getStereoisomerCode()) : null
                , srsCompound.getSaltCode() != null ? dictionaryService.byId(srsCompound.getSaltCode()) : null
                , srsCompound.getSaltEQ100()
                , SampleSource.SRS, sample.getCompoundKey()
                , sample.getChemicalName()
        );
        return compound;
    }
}
