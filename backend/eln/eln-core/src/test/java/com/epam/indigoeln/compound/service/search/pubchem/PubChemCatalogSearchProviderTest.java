package com.epam.indigoeln.compound.service.search.pubchem;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.service.search.SampleSearchService;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.epam.indigoeln.compound.model.search.SearchCatalog.PUBCHEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
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
    void setUp() {
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/rest/pug/compound/name/property/MolecularFormula,MolecularWeight,IUPACName/JSON")).willReturn(WireMock.aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(ModelUtil.loadResource("/com/epam/indigoeln/compound/service/search/pubchem-response.json"))
        ));
        wireMock.register(WireMock.get(WireMock.urlPathEqualTo("/rest/pug/compound/cid/996/property/InChI/JSON")).willReturn(WireMock.aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(ModelUtil.loadResource("/com/epam/indigoeln/compound/service/search/pubchem-inchi.json"))
        ));
    }

    @Test
    void testSearch() {
        Page<SampleDTO> result = sampleSearchService.search(new FindSamplesRequest().withCatalog(PUBCHEM).withQuickSearch("aspirin"), Paging.DEFAULT);
        assertThat(result.getItems()).hasSize(10)
                .first().satisfies(s -> {
                    assertThat(s.getChemicalName()).isNotNull();
                });
    }

    @Test
    void testImportSample() {
        Page<SampleDTO> result = provider.search(new FindSamplesRequest().withQuickSearch("aspirin"), Paging.DEFAULT);
        CompoundEntity compound = sampleSearchService.importCompound(result.getItems().getFirst());
        assertThat(compound.getId()).isNotNull();
        assertThat(compound.getCompoundKey()).isNotNull();
    }

    @Test
    void testSearchReturnsEmptyListOnNotFound() {
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/rest/pug/compound/name/property/MolecularFormula,MolecularWeight,IUPACName/JSON"))
                .withRequestBody(WireMock.containing("name=unknownxyz"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(ModelUtil.loadResource("/com/epam/indigoeln/compound/service/search/pubchem-not-found.json"))
                ));

        Page<SampleDTO> result = sampleSearchService.search(
                new FindSamplesRequest().withCatalog(PUBCHEM).withQuickSearch("unknownxyz"), Paging.DEFAULT
        );

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testSearchThrowsOnServerError() {
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo("/rest/pug/compound/name/property/MolecularFormula,MolecularWeight,IUPACName/JSON"))
                .withRequestBody(WireMock.containing("name=busy"))
                .willReturn(WireMock.aResponse()
                        .withStatus(503)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(ModelUtil.loadResource("/com/epam/indigoeln/compound/service/search/pubchem-server-error.json"))
                ));

        assertThatThrownBy(() -> {
            sampleSearchService.search(
                    new FindSamplesRequest().withCatalog(PUBCHEM).withQuickSearch("busy"), Paging.DEFAULT
            );
        })
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PUGREST.ServerBusy");
    }
}
