package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.model.CompoundExternalSource;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.google.common.base.Preconditions.checkNotNull;

// TODO PubChem suggests using throttling at 5 requests/second from an application; need Redis for this
@Slf4j
@ApplicationScoped
class PubChemCatalogSearchProvider implements CatalogSearchProvider {

    @ConfigProperty(name = "quarkus.rest-client.pubchem.uri")
    String baseUrl;

    @Inject
    @RestClient
    PubChemClient pubChemClient;

    @Inject
    PubChemMapper pubChemMapper;

    @Inject
    IndigoAPI indigo;

    @Inject
    IndigoRendererAPI indigoRenderer;

    @Inject
    CompoundService compoundService;

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.PUBCHEM;
    }

    @Override
    public CatalogSearchResult search(FindSamplesRequest request, @Nullable String nextAfter, int limit) {
        try {
            List<SampleDTO> list = executeQuery(request, limit);
            return new CatalogSearchResult(list, null, null);
        } catch (Exception e) {
            throw new RuntimeException("PubChem search failed: " + e.getMessage(), e);
        }
    }

    private List<SampleDTO> executeQuery(FindSamplesRequest searchRequest, int limit) {
        Set<String> conditions = new HashSet<>();
        Map<String, Object> queryParams = new LinkedHashMap<>();
        Map<String, Object> formParams = new LinkedHashMap<>();
        if (searchRequest.getQuickSearch() != null) {
            conditions.add("name");
            formParams.put("name", searchRequest.getQuickSearch().trim().toLowerCase());
        }
        if (searchRequest.getStructure() != null) {
            String queryType = switch (searchRequest.getStructure().type()) {
                case EXACT -> "fastidentity";
                case SUBSTRUCTURE -> "fastsubstructure";
                case SIMILARITY -> "fastsimilarity_2d";
            };
            conditions.add(queryType + "/sdf");
            formParams.put("sdf", searchRequest.getStructure().query());
            queryParams.put("MaxRecords", limit);
        }
        validate(searchRequest.getCompoundKey() == null, "For PubChem, Compound Key search is not supported");
        validate(searchRequest.getNbkBatchNumber() == null, "For PubChem, Notebook Batch Number search is not supported");
        validate(searchRequest.getCasNumber() == null, "For PubChem, CAS number search is not supported");
        validate(searchRequest.getExternalNumber() == null, "For PubChem, External Number search is not supported");
        if (searchRequest.getMolecularFormula() != null) {
            conditions.add("fastformula/" + URLEncoder.encode(searchRequest.getMolecularFormula().value().trim(), StandardCharsets.UTF_8));
            queryParams.put("MaxRecords", limit);
        }
        validate(searchRequest.getMolWeight() == null, "For PubChem, Molecular Weight search is not supported");
        validate(searchRequest.getChemicalName() == null, "For PubChem, Chemical Name search is not supported");
        validate(searchRequest.getCompoundState() == null, "For PubChem, Compound State search is not supported");
        validate(searchRequest.getBatchComment() == null, "For PubChem, Batch Comment search is not supported");
        validate(searchRequest.getHealthHazards() == null, "For PubChem, Health Hazards search is not supported");
        validate(conditions.size() == 1, "For PubChem, only single condition searches are supported");
        URI url = URI.create(baseUrl + conditions.iterator().next());
        log.debug("Search: url={}, formParams={}, queryParams={}", url, formParams, queryParams);
        List<PubChemResponse.Item> items = pubChemClient.search(url, queryParams, new MultivaluedHashMap<>(formParams)).propertyTable().items();
        indigoRenderer.setRenderOptions("svg", 300, 200);
        return pubChemMapper.mapSamples(items);
    }

    @Override
    public SampleEntity importSample(SampleDTO searchItem) {
        IndigoMolecule molecule = indigo.loadMolecule(checkNotNull(searchItem.getInchi()));
        CompoundEntity compound = compoundService.findOrCreate(molecule, null, null, null, c -> {
            c.setExternalSource(CompoundExternalSource.PUBCHEM);
            c.setExternalNumber(searchItem.getCompoundKey());
        });
        return compoundService.findOrCreateDefaultSample(compound);
    }
}
