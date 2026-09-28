package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.MarkedSampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.SampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class SampleSearchService {

    private static final int DEFAULT_PAGE_SIZE = 1000;

    private final Map<SearchCatalog, CatalogSearchProvider> providers;

    @Inject
    SampleRepository sampleRepository;
    @Inject
    SampleMapper sampleMapper;
    @Inject
    UserService userService;
    @Inject
    GlobalSearchService globalSearchService;
    @Inject
    DictionaryService dictionaryService;

    @Inject
    SampleSearchService(Instance<CatalogSearchProvider> providers) {
        this(StreamEx.of(providers.handlesStream())
                .map(Instance.Handle::get)
                .toList()
        );
    }

    SampleSearchService(List<CatalogSearchProvider> providers) {
        this.providers = StreamEx.of(providers)
                .mapToEntry(CatalogSearchProvider::catalog, Function.identity())
                .toMap();
    }

    public Page<SampleDTO> search(FindSamplesRequest request, Paging paging) {
        log.debug("search: {}, paging={}", request, paging);
        CatalogSearchProvider provider = providers.get(request.getCatalog());
        return provider.search(request, paging);
    }

    public CompoundEntity importCompound(SampleDTO sample) {
        CatalogSearchProvider provider = providers.get(sample.getCatalog());
        checkState(provider != null);
        return provider.importCompound(sample);
    }

    public SampleDTO markSample(SampleDTO sample) {
        CompoundEntity compound = importCompound(sample);
        MarkedSampleEntity markedSample = sampleRepository.findByKey(userService.getCurrentUserEntity(), sample.getSource(), sample.getSampleKey());
        if (markedSample == null) {
            markedSample = sampleMapper.markedSampleFromDTO(sample, userService.getCurrentUserEntity(), Instant.now(), compound, dictionaryService.lookup(sample.getCompoundState()));
            markedSample.setSearchVector(globalSearchService.collectSampleSearchVector(markedSample));
            sampleRepository.persist(markedSample);
        }
        return sampleMapper.markedSampleToDTO(markedSample);
    }

    public SampleDTO unmarkSample(SampleDTO sample) {
        MarkedSampleEntity markedSample = sampleRepository.findByKey(userService.getCurrentUserEntity(), sample.getSource(), sample.getSampleKey());
        if (markedSample != null) {
            sampleRepository.delete(markedSample);
        }
        sample.setMarked(false);
        return sample;
    }
}
