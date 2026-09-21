package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.compound.mapper.SRSMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationClient;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
class SampleRegistrationServiceSearchProvider extends AbstractPagedCatalogSearchProvider {

    @Inject
    @RestClient
    SampleRegistrationClient sampleRegistrationClient;
    @Inject
    SRSMapper mapper;

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.SAMPLE_REGISTRATION_SERVICE;
    }

    @Override
    protected Page<SampleDTO> doSearch(FindSamplesRequest request, int pageNo, int pageSize) {
        SRSFindSamplesRequest srsRequest = new SRSFindSamplesRequest();
        srsRequest.setQuickSearch(request.getQuickSearch());
        srsRequest.setStructure(request.getStructure());
        srsRequest.setStrCodeSample(request.getExternalNumber());
        srsRequest.setMolecularFormula(request.getMolecularFormula());
        srsRequest.setMolWeight(request.getMolWeight());
        srsRequest.setChemicalName(request.getChemicalName());
        srsRequest.setCompoundState(request.getCompoundState() != null ? request.getCompoundState().getId() : null);
        srsRequest.setBatchComment(request.getBatchComment());
        srsRequest.setHealthHazards(request.getHealthHazards() != null ? request.getHealthHazards().getId() : null);
        return map(sampleRegistrationClient.find(srsRequest, pageNo, pageSize), mapper::sampleFromSRS);
    }
}
