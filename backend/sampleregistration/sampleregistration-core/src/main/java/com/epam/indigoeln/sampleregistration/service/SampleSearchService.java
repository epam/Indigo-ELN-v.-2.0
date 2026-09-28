package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.sampleregistration.entity.SRSCompoundEntity;
import com.epam.indigoeln.sampleregistration.mapper.SRSSampleMapper;
import com.epam.indigoeln.sampleregistration.model.SRSCompoundDTO;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.sampleregistration.repository.SRSCompoundRepository;
import com.epam.indigoeln.sampleregistration.repository.SRSSampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class SampleSearchService {

    @Inject
    SRSSampleRepository sampleRepository;
    @Inject
    SRSCompoundRepository compoundRepository;
    @Inject
    SRSSampleMapper sampleMapper;

    public Page<SRSSampleDTO> find(SRSFindSamplesRequest request, Paging paging) {
        return sampleRepository.find(request, paging);
    }

    public SRSCompoundDTO getCompound(STRCodeCompound strCode) {
        SRSCompoundEntity compound = compoundRepository.findByStrCode(strCode);
        if (compound == null) {
            throw new EntityNotFoundException(SRSCompoundEntity.class, strCode);
        }
        return sampleMapper.compoundToDTO(compound);
    }
}
