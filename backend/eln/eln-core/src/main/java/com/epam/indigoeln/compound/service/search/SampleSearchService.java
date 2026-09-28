package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.FindSamplesState;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.config.DataAccess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class SampleSearchService {

    private static final int DEFAULT_PAGE_SIZE = 100;

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

    public SampleSearchResult search(FindSamplesRequest request, @Nullable Integer pageSize) {
        FindSamplesState state = request.getState();
        if (state == null) {
            List<SearchCatalog> catalogs = request.getCatalogs().stream()
                    .filter(c -> providers.get(c).isEnabled(request)) // filter out catalog not suitable for this request
                    .sorted(Comparator.comparing(SearchCatalog::getPriority))
                    .toList();
            state = new FindSamplesState(catalogs, 0, pageSize != null ? pageSize : DEFAULT_PAGE_SIZE, 0L);
        }
        log.debug("search: {}, pageSize={}", request, pageSize);
        Deque<SearchCatalog> catalogs = new ArrayDeque<>(state.catalogs());
        for (;;) {
            if (catalogs.isEmpty()) { // no catalog returned any data
                log.debug("no suitable catalogs");
                return new SampleSearchResult(List.of(), state.oldCatalogsTotalItems(), null);
            }
            SearchCatalog catalog = catalogs.peekFirst();
            CatalogSearchProvider provider = providers.get(catalog);
            CatalogSearchResult catalogResult = provider.search(request, state.pageNo(), state.pageSize());
            if (!catalogResult.items().isEmpty()) {
                Long totalItems = plus(state.oldCatalogsTotalItems(), catalogResult.totalItems());
                if (catalogResult.hasNext()) { // current catalog not yet complete
                    state = new FindSamplesState(new ArrayList<>(catalogs), state.pageNo() + 1, state.pageSize(), state.oldCatalogsTotalItems());
                } else {
                    catalogs.removeFirst();
                    if (!catalogs.isEmpty()) { // current catalog complete, next catalog is available
                        state = new FindSamplesState(new ArrayList<>(catalogs), 0, state.pageSize(), totalItems);
                    } else { // current catalog complete and it was the last
                        state = null;
                    }
                }
                SampleSearchResult result = new SampleSearchResult(catalogResult.items(), totalItems, state);
                log.debug("found {} items (total {}) from {}; next={}", catalogResult.items().size(), catalogResult.totalItems(), catalog, result.next());
                return result;
            }
            catalogs.removeFirst(); // no results, proceed with the next catalog
        }
    }

    public SampleDTO importSample(SampleDTO searchItem) {
        checkArgument(searchItem.getId() == null);
        CatalogSearchProvider provider = providers.get(searchItem.getSource());
        checkState(provider != null);
        SampleEntity sample = provider.importSample(searchItem);
        return sampleMapper.sampleToDTO(sample);
    }

    @Nullable
    private static Long plus(@Nullable Long a, @Nullable Long b) {
        return a != null && b != null ? a + b : null;
    }
}
