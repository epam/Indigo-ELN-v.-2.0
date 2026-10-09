package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.CompoundRepository;
import com.epam.indigoeln.compound.repository.MarkedSampleRepository;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

@ApplicationScoped
public class MyMaterialsCatalogSearchProvider implements CatalogSearchProvider {

    @Inject
    MarkedSampleRepository markedSampleRepository;
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
        return markedSampleRepository.find(userService.getCurrentUserEntity(), request, true, paging.getPageNoOrDefault(), paging.getPageSizeOrDefault());
    }

    @Override
    public CompoundEntity importCompound(SampleDTO sample) {
        return compoundRepository.get(checkNotNull(sample.getCompoundID()));
    }
}
