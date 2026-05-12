package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.Paging;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.epam.indigoeln.compound.model.search.SearchCatalog.PUBCHEM;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@JwtSecurity
@ConnectWireMock
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class PubChemCatalogSearchProviderTest extends ELNBaseTest {

    @Inject
    SampleSearchService sampleSearchService;

    @Inject
    PubChemCatalogSearchProvider provider;

    @Inject
    ObjectMapper objectMapper;

    WireMock wireMock;

    @BeforeEach
    void setUp() throws Exception {
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/rest/pug/compound/name/property/MolecularFormula,MolecularWeight,IUPACName,InChI/JSON")).willReturn(WireMock.aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(ModelUtil.loadResource(getClass(), "/com/epam/indigoeln/compound/service/search/pubchem-response.json"))
        ));
    }

    @Test
    void testSearch() {
        SampleSearchResult result = sampleSearchService.search(new FindSamplesRequest().withCatalogs(Set.of(PUBCHEM)).withQuickSearch("aspirin"), null, null, null);
        assertThat(result.items()).hasSize(10)
                .first().satisfies(s -> {
                    assertThat(s.getInchi()).isNotNull();
                });
    }

    @Test
    void testImportSample() {
        CatalogSearchResult result = provider.search(new FindSamplesRequest().withQuickSearch("aspirin"), null, Paging.DEFAULT_PAGE_SIZE);
        SampleDTO sample = sampleSearchService.importSample(result.items().getFirst());
        assertThat(sample.getId()).isNotNull();
        assertThat(sample.getCompoundID()).isNotNull();
        assertThat(sample.getCompoundKey()).isNotNull();
    }
}
