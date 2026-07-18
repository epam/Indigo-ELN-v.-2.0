package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SampleSearchServiceUnitTest {

    private static final SearchCatalog CATALOG_A = SearchCatalog.ELN;
    private static final SearchCatalog CATALOG_B = SearchCatalog.PUBCHEM;
    private static final SearchCatalog CATALOG_C = SearchCatalog.MY_MATERIALS;

    @Mock(answer = RETURNS_SMART_NULLS)
    CatalogSearchProvider providerA;
    @Mock(answer = RETURNS_SMART_NULLS)
    CatalogSearchProvider providerB;
    @Mock(answer = RETURNS_SMART_NULLS)
    CatalogSearchProvider providerC;
    SampleSearchService service;

    @BeforeEach
    void setUp() {
        lenient().when(providerA.catalog()).thenReturn(CATALOG_A);
        lenient().when(providerB.catalog()).thenReturn(CATALOG_B);
        lenient().when(providerC.catalog()).thenReturn(CATALOG_C);
        lenient().when(providerA.isEnabled(any())).thenReturn(true);
        lenient().when(providerB.isEnabled(any())).thenReturn(true);
        lenient().when(providerC.isEnabled(any())).thenReturn(false);

        service = new SampleSearchService(List.of(providerA, providerB, providerC));
    }

    @Test
    void noResultsFromCatalogA() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples(), 0L, false));

        SampleSearchResult result = service.search(request, 10);

        assertThat(result.items()).isEmpty();
        assertThat(result.next()).isNull();
        assertThat(result.totalItems()).isEqualTo(0L);
    }

    @Test
    void onePageFromA() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples("#1", "#2"), 2L, false));

        SampleSearchResult result = service.search(request, 10);

        assertThat(result.items()).isEqualTo(samples("#1", "#2"));
        assertThat(result.next()).isNull();
        assertThat(result.totalItems()).isEqualTo(2L);
    }

    @Test
    void twoPagesFromAWithNextPage() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples("#1", "#2"), 3L, true));
        when(providerA.search(request, 1, 10))
                .thenReturn(new CatalogSearchResult(samples("#3"), 3L, false));

        SampleSearchResult result1 = service.search(request, 10);

        assertThat(result1.items()).isEqualTo(samples("#1", "#2"));
        assertThat(result1.next()).isNotNull();
        assertThat(result1.next().catalogs()).containsExactly(CATALOG_A);
        assertThat(result1.next().pageNo()).isEqualTo(1);
        assertThat(result1.totalItems()).isEqualTo(3L);

        request.setState(result1.next());
        SampleSearchResult result2 = service.search(request, 10);

        assertThat(result2.items()).isEqualTo(samples("#3"));
        assertThat(result2.next()).isNull();
        assertThat(result2.totalItems()).isEqualTo(3L);
    }

    @Test
    void twoPagesFromAAndNextCatalog() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A, CATALOG_B));
        when(providerA.search(request, 0, 2))
                .thenReturn(new CatalogSearchResult(samples("#1", "#2"), 3L, true));
        when(providerA.search(request, 1, 2))
                .thenReturn(new CatalogSearchResult(samples("#3"), 3L, false));
        when(providerB.search(request, 0, 2))
                .thenReturn(new CatalogSearchResult(samples("#A", "#B"), null, false));

        SampleSearchResult result1 = service.search(request, 2);

        assertThat(result1.items()).isEqualTo(samples("#1", "#2"));
        assertThat(result1.next()).isNotNull();
        assertThat(result1.next().catalogs()).containsExactly(CATALOG_A, CATALOG_B);
        assertThat(result1.next().pageNo()).isEqualTo(1);
        assertThat(result1.totalItems()).isEqualTo(3L);

        request.setState(result1.next());
        SampleSearchResult result2 = service.search(request, 2);

        assertThat(result2.items()).isEqualTo(samples("#3"));
        assertThat(result2.next()).isNotNull();
        assertThat(result2.next().catalogs()).containsExactly(CATALOG_B);
        assertThat(result2.next().pageNo()).isEqualTo(0);
        assertThat(result2.totalItems()).isEqualTo(3L);

        request.setState(result2.next());
        SampleSearchResult result3 = service.search(request, 2);

        assertThat(result3.items()).isEqualTo(samples("#A", "#B"));
        assertThat(result3.next()).isNull();
        assertThat(result3.totalItems()).isNull();
    }

    @Test
    void noResultsFromAAndNoResultsFromB() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A, CATALOG_B));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples(), null, false));
        when(providerB.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples(), null, false));

        SampleSearchResult result1 = service.search(request, 10);

        assertThat(result1.items()).isEmpty();
        assertThat(result1.next()).isNull();
    }

    @Test
    void noResultsFromAAndOnePageFromB() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A, CATALOG_B));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples(), null, false));
        when(providerB.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples("#A", "#B"), 2L, false));

        SampleSearchResult result = service.search(request, 10);

        assertThat(result.items()).isEqualTo(samples("#A", "#B"));
        assertThat(result.next()).isNull();
        assertThat(result.totalItems()).isEqualTo(2);
    }

    @Test
    void skipDisabled() {
        FindSamplesRequest request = new FindSamplesRequest().withCatalogs(Set.of(CATALOG_A, CATALOG_C));
        when(providerA.search(request, 0, 10))
                .thenReturn(new CatalogSearchResult(samples(), 0L, false));
        SampleSearchResult result = service.search(request, 10);

        assertThat(result.items()).isEmpty();
        assertThat(result.next()).isNull();
        assertThat(result.totalItems()).isEqualTo(0L);
    }

    private static List<SampleDTO> samples(String... names) {
        return StreamEx.of(names)
                .map(name -> {
                    SampleDTO sample = new SampleDTO();
                    sample.setName(name);
                    return sample;
                })
                .toList();
    }
}
