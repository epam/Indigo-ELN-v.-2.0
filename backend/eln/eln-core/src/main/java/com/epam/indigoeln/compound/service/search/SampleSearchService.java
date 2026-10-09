package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.MarkedSampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.repository.MarkedSampleRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.SampleSource;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.map;
import static com.google.common.base.Preconditions.checkState;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class SampleSearchService {

    private final Map<SearchCatalog, CatalogSearchProvider> providers;

    @Inject
    MarkedSampleRepository markedSampleRepository;
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
        Page<SampleDTO> page = provider.search(request, paging);
        if (request.getCatalog() != SearchCatalog.MY_MATERIALS) { // external catalogs don't know what the user marked
            UserEntity user = userService.getCurrentUserEntity();
            StreamEx.of(page.getItems())
                    .groupingBy(SampleDTO::getSource)
                    .forEach((source, samples) -> {
                        Set<String> markedKeys = markedSampleRepository.findMarkedKeys(user, source, map(samples, SampleDTO::getSampleKey));
                        samples.forEach(s -> s.setMarked(markedKeys.contains(s.getSampleKey())));
                    });
        }
        return page;
    }

    public byte[] getCompoundPicture(SearchCatalog catalog, SampleSource source, UUID compoundID) {
        return providers.get(catalog).getCompoundPicture(source, compoundID);
    }

    public CompoundEntity importCompound(SampleDTO sample) {
        CatalogSearchProvider provider = providers.get(sample.getCatalog());
        checkState(provider != null);
        return provider.importCompound(sample);
    }

    public SampleDTO markSample(SampleDTO sample) {
        CompoundEntity compound = importCompound(sample);
        MarkedSampleEntity markedSample = markedSampleRepository.findByKey(userService.getCurrentUserEntity(), sample.getSource(), sample.getSampleKey());
        if (markedSample == null) {
            markedSample = sampleMapper.markedSampleFromDTO(sample, userService.getCurrentUserEntity(), Instant.now(), compound, dictionaryService.lookup(sample.getCompoundState()));
            markedSample.setSearchVector(globalSearchService.collectSampleSearchVector(markedSample));
            markedSampleRepository.persist(markedSample);
        }
        return sampleMapper.markedSampleToDTO(markedSample);
    }

    public SampleDTO unmarkSample(SampleDTO sample) {
        MarkedSampleEntity markedSample = markedSampleRepository.findByKey(userService.getCurrentUserEntity(), sample.getSource(), sample.getSampleKey());
        if (markedSample != null) {
            markedSampleRepository.delete(markedSample);
        }
        sample.setMarked(false);
        return sample;
    }
}
