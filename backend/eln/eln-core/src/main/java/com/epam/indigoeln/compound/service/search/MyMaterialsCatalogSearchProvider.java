package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.service.UserService;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyMaterialsCatalogSearchProvider implements CatalogSearchProvider {

    @Inject
    SampleRepository sampleRepository;
    @Inject
    CompoundRepository compoundRepository;
    @Inject
    UserService userService;

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.MY_MATERIALS;
    }

    @Override
    public Page<SampleDTO> search(FindSamplesRequest request, Paging paging) {
        return sampleRepository.find(userService.getCurrentUserEntity(), request, true, paging.getPageNoOrDefault(), paging.getPageSizeOrDefault());
    }

    @Override
    public CompoundEntity importCompound(SampleDTO sample) {
        CompoundEntity compound = compoundRepository.findByCompoundKey(sample.getSource(), sample.getCompoundKey());
        Preconditions.checkState(compound != null, "Compound not found for My Materials sample: %s, %s", sample.getSource(), sample.getSampleKey());
        return compound;
    }
}
