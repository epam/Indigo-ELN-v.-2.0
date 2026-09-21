package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.repository.SRSSampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class SampleSearchService {

    @Inject
    SRSSampleRepository sampleRepository;

    public Page<SRSSampleDTO> find(SRSFindSamplesRequest request, int pageNo, int pageSize) {
        return sampleRepository.find(request, pageNo, pageSize);
    }
}
