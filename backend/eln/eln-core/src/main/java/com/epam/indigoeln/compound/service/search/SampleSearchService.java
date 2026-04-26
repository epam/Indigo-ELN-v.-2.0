package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.config.TraceSegment;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.config.DataAccess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import one.util.streamex.LongStreamEx;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@DataAccess
@TraceSegment
@Transactional
@ApplicationScoped
public class SampleSearchService {

    private static final int DEFAULT_LIMIT = 100;

    private final Map<SearchCatalog, CatalogSearchProvider> providers;

    @Inject
    SampleMapper sampleMapper;

    @Inject
    SampleSearchService(Instance<CatalogSearchProvider> providers) {
        this(StreamEx.of(providers.handlesStream())
                .map(Instance.Handle::get)
                .toList()
        );
    }

    SampleSearchService(List<CatalogSearchProvider> providers) {
        this.providers = StreamEx.of(providers)
                .sorted(Comparator.comparing(x -> x.catalog().getPriority()))
                .mapToEntry(CatalogSearchProvider::catalog, Function.identity())
                .toCustomMap(LinkedHashMap::new);
    }

    public SampleSearchResult search(FindSamplesRequest request, @Nullable SearchCatalog nextCatalog, @Nullable String nextAfter, @Nullable Integer limit) {
        List<Pair<SearchCatalog, CatalogSearchProvider>> effectiveCatalogs = request.getCatalogs().stream()
                .sorted(Comparator.comparing(SearchCatalog::getPriority))
                .map(c -> Pair.of(c, providers.get(c)))
                .filter(p -> p.b().isEnabled(request)) // filter out catalog not suitable for this request
                .toList();
        Map<SearchCatalog, @Nullable Long> totalItemsPerCatalog = new EnumMap<>(SearchCatalog.class);
        for (Iterator<Pair<SearchCatalog, CatalogSearchProvider>> it = effectiveCatalogs.iterator(); ; ) {
            if (!it.hasNext()) { // no catalog returned any data
                return new SampleSearchResult(List.of(), false, null, null, calculateTotalItems(totalItemsPerCatalog));
            }
            Pair<SearchCatalog, CatalogSearchProvider> p = it.next();
            SearchCatalog catalog = p.a();
            CatalogSearchProvider provider = p.b();
            if (nextCatalog != null && catalog.getPriority() < nextCatalog.getPriority()) { // skip previously searched catalogs
                totalItemsPerCatalog.put(catalog, null);
                continue;
            }
            CatalogSearchResult result = provider.search(request, nextAfter, limit != null ? limit : DEFAULT_LIMIT);
            totalItemsPerCatalog.put(catalog, result.totalItems());
            if (result.size() != 0) {
                if (result.hasNext()) { // current catalog not yet complete
                    return new SampleSearchResult(result.items(), true, catalog, result.nextAfter(), calculateTotalItems(totalItemsPerCatalog));
                } else if (it.hasNext()) { // current catalog complete, next catalog is available
                    return new SampleSearchResult(result.items(), true, it.next().a(), null, calculateTotalItems(totalItemsPerCatalog));
                } else { // current catalog complete and it was the last
                    return new SampleSearchResult(result.items(), false, null, null, calculateTotalItems(totalItemsPerCatalog));
                }
            }
            // no results, proceed with the next catalog
        }
    }

    private static @Nullable Long calculateTotalItems(Map<SearchCatalog, @Nullable Long> totalItemsPerCatalog) {
        return !totalItemsPerCatalog.containsValue(null)
                ? LongStreamEx.of(totalItemsPerCatalog.values()).sum()
                : null;
    }

    public SampleDTO importSample(SampleDTO searchItem) {
        checkArgument(searchItem.getId() == null);
        CatalogSearchProvider provider = providers.get(searchItem.getSource());
        checkState(provider != null);
        SampleEntity sample = provider.importSample(searchItem);
        return sampleMapper.sampleToDTO(sample);
    }
}
